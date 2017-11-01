package org.yuefei.util;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utility {
    public static void stripWhiteSpacesElement(Node node) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength();) {
            Node current = list.item(i);
            if (current.getNodeType() == Node.TEXT_NODE && current.getNodeValue().startsWith("\n")) {
                node.removeChild(current);
                continue;
            }
            if (current.hasChildNodes()) {
                stripWhiteSpacesElement(current);
            }
            i++;
        }
    }

    public static String getAttributesAsString(Element node) {
        NamedNodeMap attributes = node.getAttributes();
        StringBuilder sb = new StringBuilder("");
        for (int j = 0; j < attributes.getLength(); j++) {
            sb.append("(").append(attributes.item(j).getNodeName()).append("->").append(attributes.item(j).getNodeValue()).append(")");
        }
        return sb.toString();
    }

    public static boolean hasElementAsChild(Element node) {
        return node.hasChildNodes() && node.getFirstChild().getNodeType() == Node.ELEMENT_NODE;
    }

/*    public static boolean copyIdToTermAttr(Node node) {
        Element term = (Element) node;
        if (term == null)
            return false;
        if (term.getElementsByTagName("id").getLength() == 0)
            return false;
        Element id = (Element) term.getElementsByTagName("id").item(0);
        String idVal  = id.getTextContent();
        term.setAttribute("id", idVal);
        term.removeChild(id);
        return true;
    }*/
}
