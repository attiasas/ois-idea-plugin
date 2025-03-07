package org.ois.idea.ui.views.entities;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.ui.views.OisToolView;
import org.ois.idea.utils.ProjectUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

public class EntitiesView extends OisToolView {

    private final String basePath;

    private final JTree directoryTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    private WatchService watchService;
    private Thread watchThread;

    public static EntitiesView getInstance(Project project) {
        return project.getService(EntitiesView.class);
    }

    public EntitiesView(@NotNull Project project) {
        super(project);

        this.basePath = ProjectUtils.getProjectBasePath(project).resolve("simulation").resolve("entities").toString();

        setLayout(new BorderLayout());

        JButton addButton = new JButton("Add Entity");
        addButton.addActionListener(e -> addNewEntity());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(addButton, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        rootNode = new DefaultMutableTreeNode("Entities");
        treeModel = new DefaultTreeModel(rootNode);
        directoryTree = new SimpleTree(treeModel);
        refreshDirectoryTree();

        directoryTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = directoryTree.getSelectionPath();
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        openBlueprintFile(node.getUserObject().toString());
                    }
                }
            }
        });

        add(new JBScrollPane(directoryTree), BorderLayout.CENTER);
        startDirectoryWatcher();
    }

    private void refreshDirectoryTree() {
        rootNode.removeAllChildren();
        File dir = new File(basePath);
        if (dir.exists() && dir.isDirectory()) {
            for (File subDir : Objects.requireNonNull(dir.listFiles(File::isDirectory))) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(subDir.getName());
                rootNode.add(node);
            }
        }
        treeModel.reload();
    }

    private void openBlueprintFile(String dirName) {
        if (dirName == null) return;
        Path dirPath = Paths.get(basePath, dirName);
        Path filePath = dirPath.resolve(dirName + ".blueprint.ois");

        if (!Files.exists(filePath)) {
            Messages.showErrorDialog(project, "Blueprint file not found in '" + dirName + "'. Please verify the file exists.", "File Not Found");
            return;
        }

        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(filePath.toFile());
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true);
        }
    }

    private void addNewEntity() {
        String entityName = Messages.showInputDialog(project, "Enter entity name:", "New Entity", Messages.getQuestionIcon());
        if (entityName == null || entityName.trim().isEmpty()) return;

        Path entityDir = Paths.get(basePath, entityName);
        Path entityFile = entityDir.resolve(entityName + ".blueprint.ois");

        try {
            Files.createDirectories(entityDir);
            Files.writeString(entityFile, "{}" );
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(entityDir.toFile());
            refreshDirectoryTree();
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to create entity: " + e.getMessage(), "Error");
        }
    }

    private void startDirectoryWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Paths.get(basePath).register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            watchThread = new Thread(() -> {
                while (true) {
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            SwingUtilities.invokeLater(this::refreshDirectoryTree);
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            watchThread.start();
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to watch directory: " + e.getMessage(), "Error");
        }
    }
}
