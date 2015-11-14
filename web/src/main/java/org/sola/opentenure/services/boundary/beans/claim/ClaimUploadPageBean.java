package org.sola.opentenure.services.boundary.beans.claim;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.sola.common.FileUtility;
import org.sola.common.SOLAException;
import org.sola.common.mapping.MappingManager;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageBean;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.helpers.MessagesKeys;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.Attachment;
import org.sola.cs.services.ejbs.claim.entities.AttachmentBinary;
import org.sola.cs.services.ejbs.claim.entities.Claim;
import org.sola.cs.services.boundary.transferobjects.claim.ClaimTO;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.contracts.CsGenericTranslator;
import org.sola.services.common.logging.LogUtility;

@Named
@RequestScoped
public class ClaimUploadPageBean extends AbstractBackingBean {

    @EJB
    ClaimEJBLocal claimEjb;

    @Inject
    MessageProvider msgProvider;

    @Inject
    MessageBean msg;

    @Inject
    LanguageBean langBean;

    private Part fileClaim;
    private String filePassword;

    public Part getFileClaim() {
        return fileClaim;
    }

    public void setFileClaim(Part fileClaim) {
        this.fileClaim = fileClaim;
    }

    public String getFilePassword() {
        return filePassword;
    }

    public void setFilePassword(String filePassword) {
        this.filePassword = filePassword;
    }

    public ClaimUploadPageBean() {
        super();
    }

    public void uploadClaim() {
        try {
            // Check fields
            if (fileClaim == null) {
                getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.CLAIM_UPLOAD_FILE_REQUIERD)));
                return;
            }

            // TODO: override with system setting value
            String otClaimsPath = System.getProperty("user.home") + "/sola/ot/claims/";

            // Check path exists
            File claimsFolder = new File(otClaimsPath);
            if (!claimsFolder.exists()) {
                claimsFolder.mkdirs();
            }

            String tmpFolderPath = otClaimsPath + UUID.randomUUID().toString();
            File tmpFolder = new File(tmpFolderPath);

            try {
                if (!tmpFolder.exists()) {
                    tmpFolder.mkdirs();
                }

                // Copy zip to temp folder
                String zipFilePath = tmpFolderPath + "/claim.zip";
                Files.copy(fileClaim.getInputStream(), new File(zipFilePath).toPath(), StandardCopyOption.REPLACE_EXISTING);

                ZipFile zipFile = new ZipFile(zipFilePath);

                if (zipFile.isEncrypted()) {
                    zipFile.setPassword(filePassword);
                }

                zipFile.extractAll(tmpFolderPath);

                // Get claim subdirectory
                File claimFolder = null;

                for (File f : tmpFolder.listFiles()) {
                    if (f.isDirectory()) {
                        claimFolder = f;
                        break;
                    }
                }

                if (claimFolder == null) {
                    getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.CLAIM_UPLOAD_BAD_FOLDER_STRUCTURE)));
                    return;
                }

                // Get json
                File json = new File(claimFolder.getAbsolutePath() + "/claim.json");

                // Get Attachments
                final File attachments = new File(claimFolder.getAbsolutePath() + "/attachments");

                // If json doesn't exist, throw exception
                if (!json.exists()) {
                    getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.CLAIM_UPLOAD_JSON_NOT_FOUND)));
                    return;
                }

                // Process json
                Claim claim = CsGenericTranslator.fromTO(getMapper().readValue(json, ClaimTO.class), Claim.class, null);

                // validate attachments
                if (claim.getAttachments() != null || claim.getAttachments().size() > 0) {
                    if (!attachments.exists()) {
                        getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.CLAIM_UPLOAD_BAD_FOLDER_STRUCTURE)));
                        return;
                    }

                    for (Attachment attach : claim.getAttachments()) {
                        File attachmentFile = new File(attachments.getAbsolutePath() + "/" + attach.getFileName());
                        if (!attachmentFile.exists()) {
                            getContext().addMessage(null, new FacesMessage(
                                    String.format(msgProvider.getErrorMessage(ErrorKeys.CLAIM_UPLOAD_ATTACHMENT_NOT_FOUND), attach.getFileName())));
                            return;
                        }
                    }
                }

                final Claim[] params = new Claim[]{claim};

                // Save claim
                runUpdate(new Runnable() {
                    @Override
                    public void run() {
                        LocalInfo.setBaseUrl(getApplicationUrl());
                        Claim claimToSave = params[0];

                        // process attachments first
                        if (claimToSave.getAttachments() != null || claimToSave.getAttachments().size() > 0) {
                            for (Attachment attach : claimToSave.getAttachments()) {
                                // Check attachment exists
                                AttachmentBinary existingAttach = claimEjb.getAttachment(attach.getId());
                                if (existingAttach == null) {
                                    File attachFile = new File(attachments.getAbsolutePath() + "/" + attach.getFileName());
                                    AttachmentBinary attachBinary = MappingManager.getMapper().map(attach, AttachmentBinary.class);
                                    try {
                                        attachBinary.setBody(Files.readAllBytes(attachFile.toPath()));
                                    } catch (IOException ex) {
                                        throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_READ_ATTACH_FILE);
                                    }
                                    claimEjb.saveAttachment(attachBinary);
                                }
                            }
                        }

                        // Save claim
                        params[0] = claimEjb.saveClaim(claimToSave, langBean.getLocale());
                    }
                });

                claim = params[0];

                // Make a link with claim number
                String claimLink = String.format("<a href=\"ViewClaim.xhtml?id=%s\">#%s</a>", claim.getId(), claim.getNr());
                msg.setSuccessMessage(String.format(msgProvider.getMessage(MessagesKeys.CLAIM_UPLOAD_PAGE_SUCCESS_UPLOAD), claimLink));

            } catch (ZipException e) {
                LogUtility.log("Failed to upload claim", e);
                getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.CLAIM_UPLOAD_ZIP_EXTRACTION_FAILED)));
            } catch (Exception e) {
                LogUtility.log("Failed to upload claim", e);
                getContext().addMessage(null, new FacesMessage(processException(e, langBean.getLocale()).getMessage()));
            } finally {
                // Try to delete created folder
                if (tmpFolder.exists()) {
                    FileUtility.deleteDirectory(tmpFolder);
                }
            }
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage(processException(e, langBean.getLocale()).getMessage()));
        }
    }
}
