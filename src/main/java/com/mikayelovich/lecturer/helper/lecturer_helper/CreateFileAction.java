package com.mikayelovich.lecturer.helper.lecturer_helper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.io.IOException;

public class CreateFileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        VirtualFile baseDir = VirtualFileManager.getInstance().findFileByUrl("file://" + basePath);
        if (baseDir == null) {
            return;
        }

        PsiDirectory basePsiDirectory = PsiManager.getInstance(project).findDirectory(baseDir);

        if (basePsiDirectory == null) {
            return;
        }

        createPackageAndFiles(project, basePsiDirectory, "com.example.newpackage");
    }

    private void createPackageAndFiles(Project project, PsiDirectory baseDir, String packageName) {
        String[] packageComponents = packageName.split("\\.");
        PsiDirectory currentDir = baseDir;

        for (String component : packageComponents) {
            PsiDirectory subDir = currentDir.findSubdirectory(component);
            if (subDir == null) {
                PsiDirectory finalCurrentDir = currentDir;
                subDir = WriteCommandAction
                        .runWriteCommandAction(project,(Computable<? extends PsiDirectory>) () -> finalCurrentDir.createSubdirectory(component));
            }
            currentDir = subDir;
        }

        // Create files in the final directory
        PsiDirectory targetDir = currentDir;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                createFile(targetDir, "MyClass.java", "package " + packageName + ";\n\npublic class MyClass {\n    // Your code here\n}");
                createFile(targetDir, "MyInterface.java", "package " + packageName + ";\n\npublic interface MyInterface {\n    // Your code here\n}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void createFile(PsiDirectory directory, String fileName, String content) throws IOException {
        PsiFile file = directory.findFile(fileName);
        if (file == null) {
            VirtualFile virtualFile = directory.getVirtualFile().createChildData(this, fileName);
            virtualFile.setBinaryContent(content.getBytes());
        }
    }
}

