package me.zoxouu.utils;

import java.io.*;
import java.nio.file.Path;

public class IOUtils {
    public static String readFileToString(Path filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }


    public static boolean save(String content, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write(content);
            return true;
        } catch (IOException e){
            return false;
        }

    }

    public static void copy(String sourcePath, String destinationPath) {
        Thread copyThread = new Thread(() -> {
            try (InputStream inputStream = new FileInputStream(sourcePath);
                 OutputStream outputStream = new FileOutputStream(destinationPath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        copyThread.start();
    }

    public static void copyDir(File sourceDirectory, File destinationDirectory) {
        Thread copyThread = new Thread(() -> {
            try {
                copyDirRecursive(sourceDirectory, destinationDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        copyThread.start();
    }

    private static void copyDirRecursive(File sourceDirectory, File destinationDirectory) throws IOException {
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        File[] files = sourceDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                File destination = new File(destinationDirectory, name);

                if (file.isDirectory()) {
                    copyDirRecursive(file, destination);
                } else {
                    copy(file.getAbsolutePath(), destination.getAbsolutePath());
                }
            }
        }
    }

    public static void deleteFile(String filePath) {
        Thread deleteThread = new Thread(() -> {
            try {
                deleteFileRecursive(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        deleteThread.start();
    }

    private static void deleteFileRecursive(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    deleteFileRecursive(subFile);
                }
            }
        }

        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean create(File file) throws IOException {
        File parentDir = file.getParentFile();
        boolean fileCreated = false;
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }
        return file.exists();
    }
}