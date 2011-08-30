/*
 * Copyright 2008 Nokia Siemens Networks Oyj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robotframework.jvmconnector.xml;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.laughingpanda.jretrofit.Retrofit;
import org.w3c.dom.*;

public class Document {
    private final org.w3c.dom.Document doc;

    public Document(org.w3c.dom.Document doc) {
        this.doc = doc;
    }
    
    public MyElement element(String name) {
        return element(doc, name); 
    }
    
    private List<MyElement> elements(MyElement parent, String name) {
        List<MyElement> results = new ArrayList<MyElement>();
        NodeList elements = getElements(parent, name);
        for (int i = 0; i < elements.getLength(); i++) {
            results.add(new MyElement((Element) elements.item(i)));
        }
        return results;
    }
    
    private MyElement element(Node parent, String name) {
        Element element = getFirstChild(parent, name); 
        if (element == null) {
            element = add(parent, name);
        }
        
        return new MyElement(element); 
    }

    private Element getFirstChild(Node parent, String name) {
        NodeList results = getElements(parent, name);
        if (results == null) return null;
        return (Element) results.item(0);
    }

    private NodeList getElements(Node parent, String name) {
        Queryable queryableParent = (Queryable) Retrofit.partial(parent, Queryable.class);
        return queryableParent.getElementsByTagName(name);
    }
    
    private interface Queryable {
        NodeList getElementsByTagName(String name);
    }

    private Element add(Node parent, String name) {
        Element element = createElement(name);
        parent.appendChild(element);
        return element;
    }
    
    private Element insert(Node parent, String name) {
        Element element = createElement(name);
        parent.insertBefore(element, getFirstChild(parent, name));
        return element;
    }

    private Element createElement(String tagName) {
        return doc.createElement(tagName);
    }
    
    public void printTo(PrintStream out) {
        try {
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }
    
    public class MyElement implements Element {
        private final Element elem;

        public MyElement(Element elem) {
            this.elem = elem;
        }
        
        public List<MyElement> elements(String name) {
            return Document.this.elements(this, name);
        }
        
        public MyElement element(String name) {
            return Document.this.element(this, name);
        }
        
        public void insertText(String value) {
            appendChild(doc.createTextNode(value));
        }

        public MyElement insertElement(String name) {
            return new MyElement(insert(this, name));
        }
        
        public MyElement addElement(String name) {
            return new MyElement(add(this, name));
        }
        
        public Node appendChild(Node newChild) throws DOMException {
            return elem.appendChild(newChild);
        }

        public Node cloneNode(boolean deep) {
            return elem.cloneNode(deep);
        }

        public short compareDocumentPosition(Node other) throws DOMException {
            return elem.compareDocumentPosition(other);
        }

        public String getAttribute(String name) {
            return elem.getAttribute(name);
        }

        public Attr getAttributeNode(String name) {
            return elem.getAttributeNode(name);
        }

        public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
            return elem.getAttributeNodeNS(namespaceURI, localName);
        }

        public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
            return elem.getAttributeNS(namespaceURI, localName);
        }

        public NamedNodeMap getAttributes() {
            return elem.getAttributes();
        }

        public String getBaseURI() {
            return elem.getBaseURI();
        }

        public NodeList getChildNodes() {
            return elem.getChildNodes();
        }

        public NodeList getElementsByTagName(String name) {
            return elem.getElementsByTagName(name);
        }

        public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
            return elem.getElementsByTagNameNS(namespaceURI, localName);
        }

        public Object getFeature(String feature, String version) {
            return elem.getFeature(feature, version);
        }

        public Node getFirstChild() {
            return elem.getFirstChild();
        }

        public Node getLastChild() {
            return elem.getLastChild();
        }

        public String getLocalName() {
            return elem.getLocalName();
        }

        public String getNamespaceURI() {
            return elem.getNamespaceURI();
        }

        public Node getNextSibling() {
            return elem.getNextSibling();
        }

        public String getNodeName() {
            return elem.getNodeName();
        }

        public short getNodeType() {
            return elem.getNodeType();
        }

        public String getNodeValue() throws DOMException {
            return elem.getNodeValue();
        }

        public org.w3c.dom.Document getOwnerDocument() {
            return elem.getOwnerDocument();
        }

        public Node getParentNode() {
            return elem.getParentNode();
        }

        public String getPrefix() {
            return elem.getPrefix();
        }

        public Node getPreviousSibling() {
            return elem.getPreviousSibling();
        }

        public TypeInfo getSchemaTypeInfo() {
            return elem.getSchemaTypeInfo();
        }

        public String getTagName() {
            return elem.getTagName();
        }

        public String getTextContent() throws DOMException {
            return elem.getTextContent();
        }

        public Object getUserData(String key) {
            return elem.getUserData(key);
        }

        public boolean hasAttribute(String name) {
            return elem.hasAttribute(name);
        }

        public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
            return elem.hasAttributeNS(namespaceURI, localName);
        }

        public boolean hasAttributes() {
            return elem.hasAttributes();
        }

        public boolean hasChildNodes() {
            return elem.hasChildNodes();
        }

        public Node insertBefore(Node newChild, Node refChild) throws DOMException {
            return elem.insertBefore(newChild, refChild);
        }

        public boolean isDefaultNamespace(String namespaceURI) {
            return elem.isDefaultNamespace(namespaceURI);
        }

        public boolean isEqualNode(Node arg) {
            return elem.isEqualNode(arg);
        }

        public boolean isSameNode(Node other) {
            return elem.isSameNode(other);
        }

        public boolean isSupported(String feature, String version) {
            return elem.isSupported(feature, version);
        }

        public String lookupNamespaceURI(String prefix) {
            return elem.lookupNamespaceURI(prefix);
        }

        public String lookupPrefix(String namespaceURI) {
            return elem.lookupPrefix(namespaceURI);
        }

        public void normalize() {
            elem.normalize();
        }

        public void removeAttribute(String name) throws DOMException {
            elem.removeAttribute(name);
        }

        public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
            return elem.removeAttributeNode(oldAttr);
        }

        public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
            elem.removeAttributeNS(namespaceURI, localName);
        }

        public Node removeChild(Node oldChild) throws DOMException {
            return elem.removeChild(oldChild);
        }

        public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
            return elem.replaceChild(newChild, oldChild);
        }

        public void setAttribute(String name, String value) throws DOMException {
            elem.setAttribute(name, value);
        }

        public Attr setAttributeNode(Attr newAttr) throws DOMException {
            return elem.setAttributeNode(newAttr);
        }

        public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
            return elem.setAttributeNodeNS(newAttr);
        }

        public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
            elem.setAttributeNS(namespaceURI, qualifiedName, value);
        }

        public void setIdAttribute(String name, boolean isId) throws DOMException {
            elem.setIdAttribute(name, isId);
        }

        public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
            elem.setIdAttributeNode(idAttr, isId);
        }

        public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
            elem.setIdAttributeNS(namespaceURI, localName, isId);
        }

        public void setNodeValue(String nodeValue) throws DOMException {
            elem.setNodeValue(nodeValue);
        }

        public void setPrefix(String prefix) throws DOMException {
            elem.setPrefix(prefix);
        }

        public void setTextContent(String textContent) throws DOMException {
            elem.setTextContent(textContent);
        }

        public Object setUserData(String key, Object data, UserDataHandler handler) {
            return elem.setUserData(key, data, handler);
        }
    }
}
