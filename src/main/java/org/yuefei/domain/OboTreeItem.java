package org.yuefei.domain;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.yuefei.util.Utility;

public class OboTreeItem extends TreeItem<String> {
    private boolean childrenLoaded = false;
    private boolean leafPropertyCalculated = false;
    private boolean leafNode = false;
    private Element backendNode;

    public OboTreeItem(Element node) {
        this.backendNode = node;
        String tag = node.getTagName();
        String attr = Utility.getAttributesAsString(node);
        String content = node.getTextContent();
        this.setValue(tag + attr + " : " + content);
    }

    public OboTreeItem(String value) {
        this.setValue(value);
        leafPropertyCalculated = true;
        leafNode = false;
        childrenLoaded = false;
    }

    @Override
    public boolean isLeaf() {
        if (!leafPropertyCalculated) {
            leafNode =  !Utility.hasElementAsChild(this.backendNode);
        }
        return leafNode;
    }

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (!childrenLoaded) {
            childrenLoaded =true;
            populateChildren(this);
        }
        return super.getChildren();
    }

    private void populateChildren(OboTreeItem oboTreeItem) {
        Element backend = oboTreeItem.backendNode;
        if (Utility.hasElementAsChild(backend)) {
            NodeList children = backend.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                OboTreeItem toAdd = new OboTreeItem((Element) children.item(i));
                oboTreeItem.getChildren().add(toAdd);
            }
        }
    }

    public Element getBackendNode() {
        return backendNode;
    }

    public void setBackendNode(Element backendNode) {
        this.backendNode = backendNode;
    }
}
