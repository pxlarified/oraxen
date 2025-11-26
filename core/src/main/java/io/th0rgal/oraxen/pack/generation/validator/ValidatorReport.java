package io.th0rgal.oraxen.pack.generation.validator;

import io.th0rgal.oraxen.utils.logs.Logs;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.stream.Collectors;

public class ValidatorReport {

    public enum Severity {
        INFO, WARNING, ERROR
    }

    private final List<ValidationIssue> issues;
    private final long validationTime;

    public ValidatorReport() {
        this.issues = new ArrayList<>();
        this.validationTime = System.currentTimeMillis();
    }

    public void addIssue(Severity severity, String message) {
        issues.add(new ValidationIssue(severity, message));
    }

    public void addIssue(Severity severity, String message, String filePath) {
        issues.add(new ValidationIssue(severity, message, filePath));
    }

    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public List<ValidationIssue> getIssuesBySeverity(Severity severity) {
        return issues.stream()
                .filter(issue -> issue.severity == severity)
                .collect(Collectors.toList());
    }

    public int getIssueCount(Severity severity) {
        return (int) issues.stream()
                .filter(issue -> issue.severity == severity)
                .count();
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public boolean hasErrors() {
        return getIssueCount(Severity.ERROR) > 0;
    }

    public void printReport() {
        long duration = System.currentTimeMillis() - validationTime;

        int errors = getIssueCount(Severity.ERROR);
        int warnings = getIssueCount(Severity.WARNING);
        int infos = getIssueCount(Severity.INFO);

        Logs.logInfo("─".repeat(50));
        Logs.logInfo("Resource Pack Validation Report");
        Logs.logInfo("─".repeat(50));

        if (!hasIssues()) {
            Logs.logSuccess("✓ No issues found in resource pack!");
        } else {
            if (errors > 0) {
                Logs.logError("✗ " + errors + " error(s) found");
            }
            if (warnings > 0) {
                Logs.logWarning("⚠ " + warnings + " warning(s) found");
            }
            if (infos > 0) {
                Logs.logInfo("ℹ " + infos + " info message(s)");
            }

            Logs.logInfo("");
            Logs.logInfo("Issues:");

            for (ValidationIssue issue : issues) {
                String prefix = switch (issue.severity) {
                    case ERROR -> "<red>✗ ERROR:</red>";
                    case WARNING -> "<yellow>⚠ WARNING:</yellow>";
                    case INFO -> "<blue>ℹ INFO:</blue>";
                };

                if (issue.filePath != null) {
                    Logs.logWarning("  " + prefix + " " + issue.message + " <gray>(" + issue.filePath + ")</gray>");
                } else {
                    Logs.logWarning("  " + prefix + " " + issue.message);
                }
            }
        }

        Logs.logInfo("");
        Logs.logInfo("Validation completed in " + duration + "ms");
        Logs.logInfo("─".repeat(50));
    }

    public static class ValidationIssue {
        public final Severity severity;
        public final String message;
        public final String filePath;

        public ValidationIssue(Severity severity, String message) {
            this.severity = severity;
            this.message = message;
            this.filePath = null;
        }

        public ValidationIssue(Severity severity, String message, String filePath) {
            this.severity = severity;
            this.message = message;
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return (filePath != null ? filePath + ": " : "") + message;
        }
    }
}
