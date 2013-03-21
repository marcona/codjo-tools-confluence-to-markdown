package net.codjo.tools.documentation;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import net.codjo.confluence.Attachment;
import net.codjo.util.file.FileUtil;
/**
 *
 */
class AttachmentDownloader {

    private AttachmentDownloader() {
    }


    static void downloadAttachment(Attachment attachment, File targetDirectory) throws IOException {
        BufferedInputStream source =
              new BufferedInputStream(new URL(attachment.getUrl()).openStream());
        BufferedOutputStream destination = null;
        try {
            File destFile = new File(targetDirectory + "/attachments", attachment.getFileName());
            destination = new BufferedOutputStream(new FileOutputStream(destFile));

            int bufferSize =
                  (source.available() < 1000000) ? source.available() : 1000000;
            byte[] buf = new byte[bufferSize];

            int bytesRead;
            while (source.available() != 0) {
                bytesRead = source.read(buf);

                destination.write(buf, 0, bytesRead);
            }
            destination.flush();
        }
        finally {
            closeStream(source);
            closeStream(destination);
        }
    }


    private static void closeStream(final InputStream source) {
        if (source != null) {
            try {
                source.close();
            }
            catch (IOException ex) {
                // Normalement ça passe
            }
        }
    }


    private static void closeStream(final OutputStream source) {
        if (source != null) {
            try {
                source.close();
            }
            catch (IOException ex) {
                // Normalement ça passe
            }
        }
    }


    static void makeAttachmentDirectoryIfNecessary(File targetDirectory) {
        final File attachmentsDirectory = new File(targetDirectory + "/attachments");
        if (!attachmentsDirectory.exists()) {
            attachmentsDirectory.mkdirs();
        }
        try {
            copyImageToAttachments(attachmentsDirectory, "forbidden.gif");
            copyImageToAttachments(attachmentsDirectory, "warning.gif");
            copyImageToAttachments(attachmentsDirectory, "lightbulb_on.gif");
            copyImageToAttachments(attachmentsDirectory, "lightbulb.gif");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void copyImageToAttachments(File attachmentsDirectory, String imageName)
          throws IOException, URISyntaxException {
        FileUtil.copyFile(new File(AttachmentDownloader.class.getResource("/" + imageName).toURI()),
                          new File(attachmentsDirectory, imageName));
    }
}
