package io.github.jonestimd.svgeditor.svg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.collect.ImmutableMap;
import io.github.jonestimd.svgeditor.GroupDefaults;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SvgParser {
    private final Consumer<Node> nodeConsumer;

    public SvgParser() {
        this(node -> {});
    }

    public SvgParser(Consumer<Node> nodeConsumer) {
        this.nodeConsumer = nodeConsumer;
    }

    public Collection<Node> parse(File file) throws IOException, ParserConfigurationException, SAXException {
        SvgSaxHandler handler = new SvgSaxHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.newSAXParser().parse(file, handler);
        return handler.nodes;
    }

    private class SvgSaxHandler extends DefaultHandler {
        private Group group = null;
        private Text text = null;

        private Map<String, Consumer<Attributes>> tagHandlers = ImmutableMap.<String, Consumer<Attributes>>builder()
                .put("svg", this::addGroup)
                .put("g", this::addGroup)
                .put("line", attributes -> addNode(new ShapeFactory(attributes, group).getLine()))
                .put("circle", attributes -> addNode(new ShapeFactory(attributes, group).getCircle()))
                .put("ellipse", attributes -> addNode(new ShapeFactory(attributes, group).getEllipse()))
                .put("rect", attributes -> addNode(new ShapeFactory(attributes, group).getRect()))
                .put("path", attributes -> addNode(new ShapeFactory(attributes, group).getPath()))
                .put("polygon", attributes -> addNode(new ShapeFactory(attributes, group).getPolygon()))
                .put("polyline", attributes -> addNode(new ShapeFactory(attributes, group).getPolyline()))
                .put("image", attributes -> new ShapeFactory(attributes, group).getImage().ifPresent(this::addNode))
                .build();

        private List<Node> nodes = new ArrayList<>();
        private boolean inDefs = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (uri.equals("http://www.w3.org/2000/svg")) {
                if ("defs".equals(qName)) inDefs = true;
                else if (!inDefs) {
                    if (tagHandlers.containsKey(qName)) {
                        tagHandlers.get(qName).accept(attributes);
                    }
                    else if ("text".equals(qName)) {
                        text = new ShapeFactory(attributes, group).getText();
                        addNode(text);
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (uri.equals("http://www.w3.org/2000/svg")) {
                if ("defs".equals(qName)) inDefs = false;
                else if (!inDefs) {
                    if ("g".equals(qName)) this.group = (Group) this.group.getParent();
                    else if ("text".equals(qName)) this.text = null;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (this.text != null) {
                StringBuilder buffer = new StringBuilder(text.getText());
                text.setText(buffer.append(new String(ch, start, length)).toString());
            }
        }

        private void addGroup(Attributes attributes) {
            Group group = new Group();
            TransformParser.setTransform(group, attributes);
            group.setUserData(new GroupDefaults(group, attributes));
            if (this.group != null) this.group.getChildren().add(group);
            else nodes.add(group);
            this.group = group;
            nodeConsumer.accept(group);
        }

        private void addNode(Node node) {
            if (group == null) nodes.add(node);
            else group.getChildren().add(node);
            nodeConsumer.accept(node);
        }
    }
}
