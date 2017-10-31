package org.yuefei.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.yuefei.domain.OboTreeItem;
import org.yuefei.util.Utility;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Controller {
    @Value(("${file.path}")) private String path;
    @Value(("${tag.term}")) private String TERM_TAG;

    @FXML private Button downLoadBtn;
    @FXML private Button searchBtn;
    @FXML private TextField txf;
    @FXML private Label statusBar;
    @FXML private TreeView<String> treeView;

    private NodeList terms;
    private TreeItem<String> treeRoot = new TreeItem<>("obo");
    private boolean loaded = false;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ObservableList<OboTreeItem> buffer = FXCollections.observableArrayList();

    @FXML
    public void initializeTreeView(ActionEvent e) {
        if (loaded) {
            statusBar.setText("XML file already loaded.");
        }
        Task<Void> downloadTask = new LoadTask();
        executorService.execute(downloadTask);
        statusBar.setText("Loading XML File ... ...");
        treeView.setRoot(treeRoot);
        treeRoot.setExpanded(true);
        treeView.setShowRoot(false);
        treeView.refresh();

        buffer.addListener(new ListChangeListener<OboTreeItem>() {
            private int count = 0;
            @Override
            public void onChanged(Change<? extends OboTreeItem> c) {
                while (c.next()) {
                    List<OboTreeItem> added = (List<OboTreeItem>) c.getAddedSubList();
                    treeRoot.getChildren().addAll(added);
                    count += added.size();
                }
            }
        });
//        treeView.setOnMouseClicked(me -> System.out.println("Clicked"));
    }

    @FXML
    public void scrollToFoundItem(ActionEvent event) {

    }

    private class LoadTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            // step1: parse XML in DOM
            File input = new File(path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            // step2: remove everything other than term-tags
            Element root = document.getDocumentElement();
            terms = root.getElementsByTagName(TERM_TAG);
            for (int i = 0; i < terms.getLength(); i++) {
                Element currentTerm = (Element) terms.item(i);
                Utility.stripNonElement(currentTerm);
                Utility.copyIdToTermAttr(currentTerm);
                OboTreeItem item = new OboTreeItem(currentTerm);
                buffer.add(item);
            }
            return null;
        }

        @Override
        protected void succeeded() {
            statusBar.setText("XML file Loaded.");
            loaded = true;
        }

        @Override
        protected void failed() {
            statusBar.setText("Loading XML file failed!");
        }
    }
}
