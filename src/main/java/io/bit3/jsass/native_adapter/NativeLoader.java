package io.bit3.jsass.native_adapter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

final class NativeLoader {
  public static void loadLibrary() {
    try {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      File dir = File.createTempFile("libjsass-", ".d", tmpDir);
      dir.delete();
      dir.mkdir();
      dir.deleteOnExit();

      saveLibrary(dir, "sass");
      String libraryPath = saveLibrary(dir, "jsass");

      System.load(libraryPath);
    } catch (Exception exception) {
      System.err.println(exception.getMessage());
      exception.printStackTrace(System.err);
      throw new RuntimeException(exception);
    }
  }


  private static URL findLibraryResource(String library) throws UnsupportedOperationException {
    String osName = System.getProperty("os.name").toLowerCase();
    String osArch = System.getProperty("os.arch").toLowerCase();
    String platform;
    String fileExtension;

    if (osName.startsWith("win")) {
      platform = "win-x86";
      fileExtension = "dll";
    } else if (osName.startsWith("linux")) {
      fileExtension = "so";

      switch (osArch) {
        case "amd64":
        case "x86_64":
          platform = "linux-x86-64";
          break;

        case "i386":
        case "x86":
          platform = "linux-x86";
          break;

        default:
          throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
      }
    } else if (osName.startsWith("mac")) {
      platform = "darwin";
      fileExtension = "dylib";
    } else {
      throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
    }

    String resourceName = "/" + platform + "/" + library + "." + fileExtension;

    URL resource = NativeLoader.class.getResource(resourceName);

    if (null == resource) {
      throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
    }

    return resource;
  }

  private static String saveLibrary(File dir, String library) throws IOException {
    library = "lib" + library;

    URL libraryResource = findLibraryResource(library);

    String basename = FilenameUtils.getName(libraryResource.getPath());
    File file = new File(dir, basename);
    file.deleteOnExit();

    try (
        InputStream in = libraryResource.openStream();
        OutputStream out = new FileOutputStream(file);
    ) {
      IOUtils.copy(in, out);
    }

    return file.getAbsolutePath();
  }
}
