package io.th0rgal.oraxen.commands;

import dev.jorel.commandapi.CommandAPICommand;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.pack.generation.validator.ResourcePackValidator;
import io.th0rgal.oraxen.pack.generation.validator.ValidatorReport;
import io.th0rgal.oraxen.utils.logs.Logs;
import io.th0rgal.oraxen.utils.VirtualFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ValidatorCommand {

    CommandAPICommand getValidatorCommand() {
        return new CommandAPICommand("validate")
                .withPermission("oraxen.command.validate")
                .executes((sender, args) -> {
                    sender.sendMessage("Starting resource pack validation...");

                    File packFile = new File(OraxenPlugin.get().getDataFolder(), "pack/pack.zip");
                    
                    if (!packFile.exists()) {
                        sender.sendMessage("<red>Resource pack file not found. Please generate the pack first with /oraxen pack generate</red>");
                        return;
                    }

                    try {
                        List<VirtualFile> files = extractFilesFromZip(packFile);
                        ResourcePackValidator validator = new ResourcePackValidator(files);
                        ValidatorReport report = validator.validate();

                        report.printReport();

                        if (report.hasErrors()) {
                            int errorCount = report.getIssueCount(ValidatorReport.Severity.ERROR);
                            sender.sendMessage("<red>Validation found " + errorCount + " error(s). Please fix them before releasing the resource pack.</red>");
                        } else if (report.hasIssues()) {
                            int warningCount = report.getIssueCount(ValidatorReport.Severity.WARNING);
                            sender.sendMessage("<yellow>Validation found " + warningCount + " warning(s). Review them for potential issues.</yellow>");
                        } else {
                            sender.sendMessage("<green>Resource pack validation passed with no issues!</green>");
                        }
                    } catch (Exception e) {
                        Logs.logError("Error during validation: " + e.getMessage());
                        e.printStackTrace();
                        sender.sendMessage("<red>Validation failed with an error. Check console for details.</red>");
                    }
                });
    }

    private List<VirtualFile> extractFilesFromZip(File zipFile) throws Exception {
        List<VirtualFile> files = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] content = zis.readAllBytes();
                    files.add(new SimpleVirtualFile(entry.getName(), content));
                }
            }
        }
        return files;
    }

    private static class SimpleVirtualFile extends VirtualFile {
        private final String path;
        private final byte[] content;

        SimpleVirtualFile(String path, byte[] content) {
            super("", path, new java.io.ByteArrayInputStream(content));
            this.path = path;
            this.content = content;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }
    }
}
