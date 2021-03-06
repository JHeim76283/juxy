package org.tigris.juxy.util;

import org.w3c.dom.*;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Pavel Sher
 */
public class XMLComparator {

  public static void assertEquals(String expected, String actual) throws DocumentsAssertionError, SAXException {
    ArgumentAssert.notNull(expected, "Expected document must not be null");
    ArgumentAssert.notEmpty(actual, "Actual document must not be empty");

    assertEquals(DOMUtil.parse(expected), DOMUtil.parse(actual));
  }

  public static void assertEquals(String expected, Node actual) throws DocumentsAssertionError, SAXException {
    ArgumentAssert.notNull(expected, "Expected document must not be null");
    ArgumentAssert.notNull(actual, "Actual document must not be null");

    assertEquals(DOMUtil.parse(expected), actual);
  }

  public static void assertEquals(Node expected, Node actual) throws DocumentsAssertionError {
    ArgumentAssert.notNull(expected, "Expected document must not be null");
    ArgumentAssert.notNull(actual, "Actual document must not be null");

    Document expectedDoc = expected.getNodeType() == Node.DOCUMENT_NODE ? (Document) expected : expected.getOwnerDocument();
    TreeWalker expTw = ((DocumentTraversal) expectedDoc).createTreeWalker(expectedDoc, NodeFilter.SHOW_ALL, new ComparatorNodeFilter(), true);
    Document actualDoc = actual.getNodeType() == Node.DOCUMENT_NODE ? (Document) actual : actual.getOwnerDocument();
    TreeWalker actualTw = ((DocumentTraversal) actualDoc).createTreeWalker(actualDoc, NodeFilter.SHOW_ALL, new ComparatorNodeFilter(), true);

    Node enode = skipDocumentIfNeeded(expected);
    Node anode = skipDocumentIfNeeded(actual);
    if (enode == null && anode == null) return;
    if (enode == null || anode == null)
      throw new DocumentsAssertionError(expTw, actualTw);

    expTw.setCurrentNode(enode);
    actualTw.setCurrentNode(anode);

    while (true) {
      if (enode == null && (anode == null || anode == actual.getNextSibling())) return;
      if (enode == null || anode == null)
        throw new DocumentsAssertionError(expTw, actualTw);

      if (enode.getNodeType() != anode.getNodeType())
        throw new DocumentsAssertionError(expTw, actualTw);

      enode.normalize();
      anode.normalize();

      switch (enode.getNodeType()) {
        case Node.TEXT_NODE:
        case Node.COMMENT_NODE:
        case Node.CDATA_SECTION_NODE:
          checkNodeValues(enode, anode, expTw, actualTw);
          break;
        case Node.ELEMENT_NODE:
          checkNodeNames(enode, anode, expTw, actualTw);
          checkNodeNamespaces(enode, anode, expTw, actualTw);
          checkAttributes((Element) enode, (Element) anode, expTw, actualTw);
          break;
        case Node.PROCESSING_INSTRUCTION_NODE:
          checkNodeNames(enode, anode, expTw, actualTw);
          checkNodeValues(enode, anode, expTw, actualTw);
          break;
        case Node.DOCUMENT_TYPE_NODE:
          checkNodeNames(enode, anode, expTw, actualTw);
          DocumentType edt = (DocumentType) enode;
          DocumentType adt = (DocumentType) anode;
          checkStringsEqual(edt.getInternalSubset(), adt.getInternalSubset(), expTw, actualTw);
          checkStringsEqual(edt.getSystemId(), adt.getSystemId(), expTw, actualTw);
          checkStringsEqual(edt.getPublicId(), adt.getPublicId(), expTw, actualTw);
          break;
      }

      enode = expTw.nextNode();
      anode = actualTw.nextNode();
    }
  }

  private static Node skipDocumentIfNeeded(Node startFrom) {
    while (true && startFrom != null) {
      switch (startFrom.getNodeType()) {
        case Node.DOCUMENT_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
          startFrom = startFrom.getFirstChild();
          break;
        case Node.TEXT_NODE:
          if (startFrom.getNodeValue().trim().length() == 0) {
            startFrom = startFrom.getNextSibling();
          }
          break;
        default:
          return startFrom;
      }
    }

    return startFrom;
  }

  private static void checkAttributes(Element enode, Element anode, TreeWalker expTw, TreeWalker actualTw) {
    NamedNodeMap expattrs = enode.getAttributes();
    NamedNodeMap actattrs = anode.getAttributes();

    Map efiltered = filterAttributes(expattrs);
    Map afiltered = filterAttributes(actattrs);

    if (efiltered.size() != afiltered.size())
      throw new DocumentsAssertionError(expTw, actualTw);

    Iterator attrsIt = efiltered.entrySet().iterator();
    while (attrsIt.hasNext()) {
      Map.Entry aentry = (Map.Entry) attrsIt.next();
      String attrName = (String) aentry.getKey();
      Node attr = (Node) aentry.getValue();
      Node actual = (Node) afiltered.get(attrName);
      if (actual == null)
        throw new DocumentsAssertionError(expTw, actualTw);

      checkNodeNamespaces(attr, actual, expTw, actualTw);
      checkNodeValues(attr, actual, expTw, actualTw);
    }
  }

  /**
   * Removes all xmlns attributes
   *
   * @return Map
   */
  private static Map filterAttributes(NamedNodeMap attrs) {
    Map filtered = new HashMap();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node attr = attrs.item(i);
      if (!attr.getNodeName().startsWith("xmlns"))
        filtered.put(attr.getNodeName(), attr);
    }
    return filtered;
  }

  private static void checkNodeNamespaces(Node enode, Node anode, TreeWalker expTw, TreeWalker actualTw) {
    checkStringsEqual(enode.getNamespaceURI(), anode.getNamespaceURI(), expTw, actualTw);
  }

  private static void checkNodeNames(Node enode, Node anode, TreeWalker expTw, TreeWalker actualTw) {
    String enodeName = enode.getLocalName() == null ? enode.getNodeName() : enode.getLocalName();
    String anodeName = anode.getLocalName() == null ? anode.getNodeName() : anode.getLocalName();

    if (!enodeName.equals(anodeName))
      throw new DocumentsAssertionError(expTw, actualTw);
  }

  private static void checkNodeValues(Node enode, Node anode, TreeWalker expTw, TreeWalker actualTw) {
    String eval = enode.getNodeValue().trim();
    String aval = anode.getNodeValue().trim();
    if (!eval.equals(aval))
      throw new DocumentsAssertionError(expTw, actualTw);
  }

  private static void checkStringsEqual(String estr, String astr, TreeWalker expTw, TreeWalker actualTw) {
    if (estr != null && astr != null) {
      if (!estr.equals(astr))
        throw new DocumentsAssertionError(expTw, actualTw);
    } else if ((estr != null && estr.length() > 0) || (astr != null && astr.length() > 0))
      throw new DocumentsAssertionError(expTw, actualTw);
  }

  static class ComparatorNodeFilter implements NodeFilter {
    public short acceptNode(Node n) {
      switch (n.getNodeType()) {
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.ENTITY_NODE:
        case Node.ENTITY_REFERENCE_NODE:
        case Node.NOTATION_NODE:
          // parsed entities will be automatically expanded,
          // notations are not supported
          return NodeFilter.FILTER_SKIP;
        case Node.TEXT_NODE:
          if (n.getNodeValue().trim().length() == 0)
            return NodeFilter.FILTER_SKIP;
          break;
      }

      return NodeFilter.FILTER_ACCEPT;
    }
  }
}
