package fr.arthurbambou.fdlink.compat_1_16;

import fr.arthurbambou.fdlink.versionhelpers.minecraft.Message;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.MessagePacket;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.MinecraftServer;
import fr.arthurbambou.fdlink.versionhelpers.minecraft.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Text;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MinecraftServer1_16 implements MinecraftServer {

    private final net.minecraft.server.MinecraftServer minecraftServer;

    public MinecraftServer1_16(net.minecraft.server.MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    @Override
    public String getMotd() {
        return this.minecraftServer.getServerMotd();
    }

    @Override
    public int getPlayerCount() {
        return this.minecraftServer.getPlayerManager().getPlayerList().size();
    }

    @Override
    public int getMaxPlayerCount() {
        return this.minecraftServer.getPlayerManager().getMaxPlayerCount();
    }

    @Override
    public List<PlayerEntity> getPlayers() {
        List<PlayerEntity> list = new ArrayList<>();
        for (ServerPlayerEntity playerEntity : this.minecraftServer.getPlayerManager().getPlayerList()) {
            list.add(new PlayerEntity1_16(playerEntity));
        }
        return list;
    }

    @Override
    public void sendMessageToAll(MessagePacket messagePacket) {
        Message message = messagePacket.getMessage();
        MutableText text = null;
        if (message.getType() == Message.MessageObjectType.STRING) {
            text = new LiteralText(message.getMessage());
        } else {
            if (message.getTextType() == Message.TextType.LITERAL) {
                Parser parser = Parser.builder()
                        .extensions(Arrays.asList(StrikethroughExtension.create()))
                        .customDelimiterProcessor(new SpoilerDelimiterProcessor())
                        .build();
                Node document = parser.parse(message.getMessage());
                MessageVisitor visitor = new MessageVisitor();
                document.accept(visitor);

                text = visitor.text;
            } else if (message.getTextType() == Message.TextType.TRANSLATABLE) {
                text = new TranslatableText(message.getKey(), message.getArgs());
            }
        }

        // Get general style
        Style vanillaStyle = Style.EMPTY;
        fr.arthurbambou.fdlink.versionhelpers.minecraft.style.Style compatStyle = message.getStyle();
        vanillaStyle = vanillaStyle
                .withBold(compatStyle.isBold())
                .withInsertion(compatStyle.getInsertion())
                .withItalic(compatStyle.isItalic())
                /*.withFont(new Identifier(compatStyle.getFont()))*/;
        if (compatStyle.isObfuscated()) vanillaStyle = vanillaStyle.withFormatting(Formatting.OBFUSCATED);
        if (compatStyle.isStrikethrough()) vanillaStyle = vanillaStyle.withFormatting(Formatting.STRIKETHROUGH);
        if (compatStyle.isUnderlined()) vanillaStyle = vanillaStyle.withFormatting(Formatting.UNDERLINE);
        if (compatStyle.getClickEvent() != null) {
            vanillaStyle = vanillaStyle.withClickEvent(new ClickEvent(ClickEvent.Action.byName(compatStyle.getClickEvent().getAction().getName()),
                    compatStyle.getClickEvent().getValue()));
        }
        if (compatStyle.getColor() != null) {
            vanillaStyle = vanillaStyle.withColor(TextColor.fromRgb(compatStyle.getColor().getRgb()));
        }
        text.setStyle(vanillaStyle);
        this.minecraftServer.getPlayerManager().sendToAll(new GameMessageS2CPacket(text, getMessageType(messagePacket.getMessageType()), messagePacket.getUUID()));
    }

    @Override
    public File getIcon() {
        return this.minecraftServer.getFile("server-icon.png");
    }

    @Override
    public PlayerEntity getPlayerFromUsername(String username) {
        return new PlayerEntity1_16(this.minecraftServer.getPlayerManager().getPlayer(username));
    }

    @Override
    public String getUsernameFromUUID(UUID uuid) {
        return this.minecraftServer.getPlayerManager().getPlayer(uuid).getName().getString();
    }

    private MessageType getMessageType(MessagePacket.MessageType messageType) {
        switch (messageType) {
            case INFO:
                return MessageType.GAME_INFO;
            case SYSTEM:
                return MessageType.SYSTEM;
            default:
                return MessageType.CHAT;
        }
    }

    @Override
    public String getIp() {
        return this.minecraftServer.getServerIp();
    }

    class MessageVisitor implements Visitor {
        private LiteralText text = new LiteralText("");
        private Style style = Style.EMPTY;

        @Override
        public void visit(BlockQuote blockQuote)
        {
            System.out.println(blockQuote);
        }

        @Override
        public void visit(BulletList bulletList)
        {
            System.out.println(bulletList);
        }

        @Override
        public void visit(Code code)
        {
            System.out.println(code);
        }

        @Override
        public void visit(Document document)
        {
            Node child = document.getFirstChild();
            do {
                child.accept(this);
            } while ((child = child.getNext()) != null);
        }

        @Override
        public void visit(Emphasis emphasis)
        {
            style = style.withItalic(true);
            System.out.println(emphasis);

            Node child = emphasis.getFirstChild();
            do {
                child.accept(this);
            } while ((child = child.getNext()) != null);

            style = style.withItalic(false);
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock)
        {
            System.out.println(fencedCodeBlock);
        }

        @Override
        public void visit(HardLineBreak hardLineBreak)
        {
            System.out.println(hardLineBreak);
        }

        @Override
        public void visit(Heading heading)
        {
            System.out.println(heading);
        }

        @Override
        public void visit(ThematicBreak thematicBreak)
        {
            System.out.println(thematicBreak);
        }

        @Override
        public void visit(HtmlInline htmlInline)
        {
            System.out.println(htmlInline);
        }

        @Override
        public void visit(HtmlBlock htmlBlock)
        {
            System.out.println(htmlBlock);
        }

        @Override
        public void visit(Image image)
        {
            System.out.println(image);
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock)
        {
            System.out.println(indentedCodeBlock);
        }

        @Override
        public void visit(Link link)
        {
            style = style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.getDestination()));
            System.out.println(link);

            Node child = link.getFirstChild();
            do {
                child.accept(this);
            } while ((child = child.getNext()) != null);

            style = style.withClickEvent(null);
        }

        @Override
        public void visit(ListItem listItem)
        {
            System.out.println(listItem);
        }

        @Override
        public void visit(OrderedList orderedList)
        {
            System.out.println(orderedList);
        }

        @Override
        public void visit(Paragraph paragraph)
        {
            System.out.println(paragraph);

            Node child = paragraph.getFirstChild();
            do {
                child.accept(this);
            } while ((child = child.getNext()) != null);
        }

        @Override
        public void visit(SoftLineBreak softLineBreak)
        {
            System.out.println(softLineBreak);
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis)
        {
            style = style.withBold(true);
            System.out.println(strongEmphasis);

            Node child = strongEmphasis.getFirstChild();
            do {
                child.accept(this);
            } while ((child = child.getNext()) != null);

            style = style.withBold(false);
        }

        @Override
        public void visit(Text text)
        {
            System.out.println(text);
            LiteralText append = new LiteralText(text.getLiteral());
            append.setStyle(style);
            this.text.append(append);
        }

        @Override
        public void visit(LinkReferenceDefinition linkReferenceDefinition)
        {
            System.out.println(linkReferenceDefinition);
        }

        @Override
        public void visit(CustomBlock customBlock)
        {
            System.out.println(customBlock);
        }

        @Override
        public void visit(CustomNode customNode)
        {
            Style old = style;
            if (customNode instanceof Strikethrough) {
                style = style.withFormatting(Formatting.STRIKETHROUGH);
            } else if (customNode instanceof Spoiler) {
                style = style.withFormatting(Formatting.OBFUSCATED);
            }

            System.out.println(customNode);
            Node child = customNode.getFirstChild();
            do {
                child.accept(this);
            } while ((child = child.getNext()) != null);

            style = old;
        }
    }

    class SpoilerDelimiterProcessor implements DelimiterProcessor {
        @Override
        public char getOpeningCharacter() {
            return '|';
        }

        @Override
        public char getClosingCharacter() {
            return '|';
        }

        @Override
        public int getMinLength() {
            return 2;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            if (openingRun.length() >= 2 && closingRun.length() >= 2) {
                // Use exactly two delimiters even if we have more, and don't care about internal openers/closers.

                Text opener = openingRun.getOpener();

                // Wrap nodes between delimiters in strikethrough.
                Node strikethrough = new Spoiler();

                SourceSpans sourceSpans = new SourceSpans();
                sourceSpans.addAllFrom(openingRun.getOpeners(2));

                for (Node node : Nodes.between(opener, closingRun.getCloser())) {
                    strikethrough.appendChild(node);
                    sourceSpans.addAll(node.getSourceSpans());
                }

                sourceSpans.addAllFrom(closingRun.getClosers(2));
                strikethrough.setSourceSpans(sourceSpans.getSourceSpans());

                opener.insertAfter(strikethrough);

                return 2;
            } else {
                return 0;
            }
        }
    }

    public class Spoiler extends CustomNode implements Delimited {
        private static final String DELIMITER = "||";

        @Override
        public String getOpeningDelimiter() {
            return DELIMITER;
        }

        @Override
        public String getClosingDelimiter() {
            return DELIMITER;
        }
    }
}
