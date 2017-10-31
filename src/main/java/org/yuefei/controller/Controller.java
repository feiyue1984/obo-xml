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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class Controller {
    @Value(("${file.path}"))
    private String pathOfDataXML;
    @Value(("${tag.term}"))
    private String termTagName;

    @FXML
    private Button downLoadBtn;
    @FXML
    private Button searchBtn;
    @FXML
    private TextField txf;
    @FXML
    private Label statusBar;
    @FXML
    private TreeView<String> treeView;

    private TreeItem<String> treeRoot = new TreeItem<>("obo");
    private boolean loaded = false;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ObservableList<OboTreeItem> buffer = FXCollections.observableArrayList();
    private final TreeMap<String, OboTreeItem> registry = new TreeMap<>();

    @FXML
    public void initializeTreeView() {
        Task<Stream<Element>> downloadTask = new LoadTask(pathOfDataXML, termTagName);
        downloadTask.setOnSucceeded(e -> {
            statusBar.setText("XML file is loaded.");
            Stream<Element> termStream = downloadTask.getValue();
            termStream
                    .forEach(term -> {
                        Element id = (Element) term.getElementsByTagName("id").item(0);
                        OboTreeItem item = new OboTreeItem(term.getTagName() + " : " + id.getTextContent());
                        item.setBackendNode(term);
                        registry.put(id.getTextContent(), item);
                        buffer.add(item);
                    });
            treeView.setRoot(treeRoot);
            treeRoot.setExpanded(true);
            treeView.setShowRoot(false);
            treeView.refresh();
        });
        downloadTask.setOnFailed(e -> statusBar.setText("Loading XML file failed."));
        downloadTask.setOnRunning(e -> {
            statusBar.setText("Loading XML file ... ...");
            downLoadBtn.setDisable(true);
        });

        executorService.execute(downloadTask);

        treeRoot.addEventHandler(TreeItem.branchExpandedEvent(), xe -> {

        });

        buffer.addListener(new ListChangeListener<OboTreeItem>() {
            private List<OboTreeItem> batch = new ArrayList<>(10000);
            @Override
            public void onChanged(Change<? extends OboTreeItem> c) {
                while (c.next() && c.wasAdded()) {
                    treeRoot.getChildren().addAll(c.getAddedSubList());
                }
            }
        });
    }

    @FXML
    public void scrollToFoundItem(ActionEvent event) {

    }

    private class LoadTask extends Task<Stream<Element>> {
        private String path;
        private String tag;
        LoadTask(String path, String tag) {
            this.path = path;
            this.tag = tag;
        }

        @Override
        protected Stream<Element> call() throws Exception {
            // step1: parse XML in DOM
            System.out.println("start load xml : " + new Date());
            File input = new File(path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            // step2: remove everything other than term-tags
            Element root = document.getDocumentElement();
            Utility.stripNonElement(root);
            NodeList termNodes = root.getElementsByTagName(tag);
            return IntStream.range(0, termNodes.getLength()).mapToObj(idx -> ((Element) termNodes.item(idx)));
        }
    }
}
