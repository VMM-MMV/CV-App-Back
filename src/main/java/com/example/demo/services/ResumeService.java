package com.example.demo.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ResumeService {
    private final Gmail serviceGmail;
    private final Drive driveService;

    public ResumeService() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        serviceGmail = new Gmail.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, jsonFactory))
                .setApplicationName("Service Gmail API")
                .build();
        driveService = new Drive.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, jsonFactory))
                .setApplicationName("Service Google Drive API")
                .build();
    }

    private Credential getCredentials(final NetHttpTransport http_Transport, GsonFactory jsonFactory)
            throws IOException {

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(Objects.requireNonNull(ResumeService.class.getResourceAsStream("/credentials.json"))));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                http_Transport, jsonFactory, clientSecrets, Set.of(GmailScopes.GMAIL_READONLY, DriveScopes.DRIVE_FILE))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void listMessagesWithAttachments() {
        try {
            ListMessagesResponse response = serviceGmail.users().messages().list("me")
                    .setMaxResults(10L)
                    .execute();

            List<Message> messages = response.getMessages();
            String lastMessageDate = readLastMessageDateFromFile();
            for (Message message : messages) {
                String messageId = message.getId();
                Message messageWithAttachments = serviceGmail.users().messages().get("me", messageId)
                        .setFormat("full")
                        .execute();

                String messageDate = String.valueOf(messageWithAttachments.getInternalDate());

                if (lastMessageDate == null || messageDate.compareTo(lastMessageDate) > 0) {
                    lastMessageDate = messageDate;
                    MessagePart payload = messageWithAttachments.getPayload();
                    if (payload != null) {
                        List<MessagePart> parts = payload.getParts();
                        if (parts != null) {
                            for (MessagePart part : parts) {
                                String filename = part.getFilename();
                                if (filename != null && (filename.endsWith(".pdf") || filename.endsWith(".docx") || filename.endsWith(".doc"))) {
                                    String attId = part.getBody().getAttachmentId();
                                    uploadResumeToDrive(messageId, attId, "1KFTjVjo4qfR5-HsRQqKSTBk7RKyO5WKe", filename);
                                }
                            }
                        }
                    }
                }
            }
            writeDateToFile(lastMessageDate);
            System.out.println("CVs have been uploaded to the drive.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadResumeToDrive(String messageId, String attachmentId, String folderId, String filename) {
        try {
            if (messageId != null && attachmentId != null) {
                MessagePartBody attachment = serviceGmail.users().messages().attachments().get("me", messageId, attachmentId).execute();
                byte[] fileByteArray = attachment.decodeData();
                saveAndUploadFile(fileByteArray, folderId, filename);
            } else {
                java.io.File tempFile = new java.io.File(filename);
                byte[] fileByteArray = Files.readAllBytes(tempFile.toPath());
                saveAndUploadFile(fileByteArray, folderId, filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAndUploadFile(byte[] fileByteArray, String folderId, String filename) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(filename);

        if (folderId != null) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }

        ByteArrayContent mediaContent = new ByteArrayContent("application/pdf", fileByteArray);

        driveService.files().create(fileMetadata, mediaContent).setSupportsTeamDrives(true).execute();
    }

    public void deleteDuplicateFilesInDrive() {
        try {
            List<File> driveFiles = getDriveFiles("1KFTjVjo4qfR5-HsRQqKSTBk7RKyO5WKe");
            Map<String, List<File>> fileMap = new HashMap<>();

            for (File file : driveFiles) {
                String fileName = file.getName();
                fileMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(file);
            }

            for (Map.Entry<String, List<File>> entry : fileMap.entrySet()) {
                List<File> duplicates = entry.getValue();
                if (duplicates.size() > 1) {
                    duplicates.sort(Comparator.comparingLong(file -> {
                        DateTime modifiedTime = file.getModifiedTime();
                        return (modifiedTime != null) ? modifiedTime.getValue() : 0L;
                    }));

                    for (int i = duplicates.size() - 1; i > 0; i--) {
                        File fileToDelete = duplicates.get(i);
                        try {
                            driveService.files().delete(fileToDelete.getId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<File> getDriveFiles(String folderId) throws IOException {
        String query = "'" + folderId + "' in parents and trashed=false";
        return driveService.files().list().setQ(query).execute().getFiles();
    }

    private String readLastMessageDateFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("lastMessageDate.txt"))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeDateToFile(String lasMessageDate) {
        try {
            FileWriter writer = new FileWriter("lastMessageDate.txt");
            writer.write(lasMessageDate);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new ResumeService().listMessagesWithAttachments();
        new ResumeService().deleteDuplicateFilesInDrive();
    }
}