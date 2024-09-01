package com.mikayelovich.lecturer.helper.lecturer_helper;

import com.intellij.ide.actions.CreateFileAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.jetbrains.annotations.NotNull;

public class CreateAndTypeInFileAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        PsiDirectory baseDir = getBaseDir(project);
        if (baseDir == null) {
            return;
        }

        createFileWithDialog(project, baseDir);
    }

    private PsiDirectory getBaseDir(Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }

        VirtualFile baseDir = project.getBaseDir();
        return PsiManager.getInstance(project).findDirectory(baseDir);
    }

    private void createFileWithDialog(Project project, PsiDirectory baseDir) {
        CreateFileAction.MkDirs mkDirs = new CreateFileAction.MkDirs("", baseDir);
        CreateFileAction action = new CreateFileAction("New Java Class", "Create new Java class", null);
        action.create(new CreateFileAction.MkDirs("", baseDir), project, (PsiDirectory) null, "MyNewClass.java", null, "public class MyNewClass {\n    // Your code here\n}", (PsiFile file) -> {
            openFileInEditor(project, file.getVirtualFile());
            return file;
        });
    }

    private void openFileInEditor(Project project, VirtualFile file) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);

            if (editor != null) {
                typeContent(editor, "public class MyNewClass {\n    // Your code here\n}");
            }
        });
    }

    private void typeContent(Editor editor, String content) {
        new Thread(() -> {
            try {
                for (char c : content.toCharArray()) {
                    Thread.sleep(333); // 3 chars per second
                    WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                        editor.getDocument().insertString(editor.getDocument().getTextLength(), String.valueOf(c));
                    });
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
