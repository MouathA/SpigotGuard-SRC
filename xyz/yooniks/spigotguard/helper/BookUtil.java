package xyz.yooniks.spigotguard.helper;

import org.bukkit.inventory.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.inventory.meta.*;
import java.util.*;
import org.bukkit.*;

public final class BookUtil
{
    public static BookBuilder writtenBook() {
        return new BookBuilder(new ItemStack(Material.WRITTEN_BOOK));
    }
    
    public static void openPlayer(final Player player, final ItemStack itemStack) {
        final CustomBookOpenEvent customBookOpenEvent = new CustomBookOpenEvent(player, itemStack, false);
        Bukkit.getPluginManager().callEvent((Event)customBookOpenEvent);
        if (customBookOpenEvent.isCancelled()) {
            return;
        }
        player.closeInventory();
        final ItemStack itemInHand = player.getItemInHand();
        player.setItemInHand(customBookOpenEvent.getBook());
        player.updateInventory();
        NmsBookHelper.openBook(player, customBookOpenEvent.getBook(), customBookOpenEvent.getHand() == CustomBookOpenEvent.Hand.OFF_HAND);
        player.setItemInHand(itemInHand);
        player.updateInventory();
    }
    
    public interface ClickAction
    {
        @Deprecated
        default ClickAction suggestCommand(final String s) {
            return new SimpleClickAction(ClickEvent.Action.SUGGEST_COMMAND, s);
        }
        
        default ClickAction openUrl(final String s) {
            if (s.startsWith("http://") || s.startsWith("https://")) {
                return new SimpleClickAction(ClickEvent.Action.OPEN_URL, s);
            }
            throw new IllegalArgumentException(String.valueOf(new StringBuilder().append("Invalid url: \"").append(s).append("\", it should start with http:// or https://")));
        }
        
        ClickEvent.Action action();
        
        String value();
        
        default ClickAction runCommand(final String s) {
            return new SimpleClickAction(ClickEvent.Action.RUN_COMMAND, s);
        }
        
        public static class SimpleClickAction implements ClickAction
        {
            private final ClickEvent.Action action;
            private final String value;
            
            @Override
            public String value() {
                return this.value;
            }
            
            public SimpleClickAction(final ClickEvent.Action action, final String value) {
                this.action = action;
                this.value = value;
            }
            
            @Override
            public ClickEvent.Action action() {
                return this.action;
            }
        }
    }
    
    public interface HoverAction
    {
        default HoverAction showItem(final ItemStack itemStack) {
            return new SimpleHoverAction(HoverEvent.Action.SHOW_ITEM, NmsBookHelper.itemToComponents(itemStack));
        }
        
        default HoverAction showText(final String s) {
            return new SimpleHoverAction(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { (BaseComponent)new TextComponent(s) });
        }
        
        HoverEvent.Action action();
        
        BaseComponent[] value();
        
        default HoverAction showItem(final BaseComponent... array) {
            return new SimpleHoverAction(HoverEvent.Action.SHOW_ITEM, array);
        }
        
        default HoverAction showStatistic(final String s) {
            return new SimpleHoverAction(HoverEvent.Action.SHOW_ACHIEVEMENT, new BaseComponent[] { (BaseComponent)new TextComponent(String.valueOf(new StringBuilder().append("statistic.").append(s))) });
        }
        
        default HoverAction showText(final BaseComponent... array) {
            return new SimpleHoverAction(HoverEvent.Action.SHOW_TEXT, array);
        }
        
        public static class SimpleHoverAction implements HoverAction
        {
            private final BaseComponent[] value;
            private final HoverEvent.Action action;
            
            public SimpleHoverAction(final HoverEvent.Action action, final BaseComponent... value) {
                this.action = action;
                this.value = value;
            }
            
            @Override
            public HoverEvent.Action action() {
                return this.action;
            }
            
            @Override
            public BaseComponent[] value() {
                return this.value;
            }
        }
    }
    
    public static class BookBuilder
    {
        private final BookMeta meta;
        private final ItemStack book;
        
        public BookBuilder title(final String title) {
            this.meta.setTitle(title);
            return this;
        }
        
        public BookBuilder pagesRaw(final List<String> pages) {
            this.meta.setPages((List)pages);
            return this;
        }
        
        public BookBuilder pages(final List<BaseComponent[]> list) {
            NmsBookHelper.setPages(this.meta, list.toArray(new BaseComponent[0][]));
            return this;
        }
        
        public BookBuilder(final ItemStack book) {
            this.book = book;
            this.meta = (BookMeta)book.getItemMeta();
        }
        
        public BookBuilder author(final String author) {
            this.meta.setAuthor(author);
            return this;
        }
        
        public BookBuilder pagesRaw(final String... pages) {
            this.meta.setPages(pages);
            return this;
        }
        
        public BookBuilder pages(final BaseComponent[]... array) {
            NmsBookHelper.setPages(this.meta, array);
            return this;
        }
        
        public ItemStack build() {
            this.book.setItemMeta((ItemMeta)this.meta);
            return this.book;
        }
    }
    
    public static class PageBuilder
    {
        private final List<BaseComponent> text;
        
        public PageBuilder newLine() {
            this.text.add((BaseComponent)new TextComponent("\n"));
            return this;
        }
        
        public PageBuilder add(final BaseComponent baseComponent) {
            this.text.add(baseComponent);
            return this;
        }
        
        public BaseComponent[] build() {
            return this.text.toArray(new BaseComponent[0]);
        }
        
        public PageBuilder() {
            this.text = new ArrayList<BaseComponent>();
        }
        
        public static PageBuilder of(final String s) {
            return new PageBuilder().add(s);
        }
        
        public static PageBuilder of(final BaseComponent baseComponent) {
            return new PageBuilder().add(baseComponent);
        }
        
        public PageBuilder add(final Collection<BaseComponent> collection) {
            this.text.addAll(collection);
            return this;
        }
        
        public PageBuilder add(final BaseComponent... array) {
            this.text.addAll(Arrays.asList(array));
            return this;
        }
        
        public static PageBuilder of(final BaseComponent... array) {
            final PageBuilder pageBuilder = new PageBuilder();
            for (int length = array.length, i = 0; i < length; ++i) {
                pageBuilder.add(array[i]);
            }
            return pageBuilder;
        }
        
        public PageBuilder add(final String s) {
            this.text.add(TextBuilder.of(s).build());
            return this;
        }
    }
    
    public static class TextBuilder
    {
        private ChatColor[] style;
        private HoverAction onHover;
        private String text;
        private ClickAction onClick;
        private ChatColor color;
        
        public TextBuilder() {
            this.text = "";
            this.onClick = null;
            this.onHover = null;
            this.color = ChatColor.BLACK;
        }
        
        public TextBuilder onClick(final ClickAction onClick) {
            this.onClick = onClick;
            return this;
        }
        
        public BaseComponent build() {
            final TextComponent textComponent = new TextComponent(this.text);
            if (this.onClick != null) {
                textComponent.setClickEvent(new ClickEvent(this.onClick.action(), this.onClick.value()));
            }
            if (this.onHover != null) {
                textComponent.setHoverEvent(new HoverEvent(this.onHover.action(), this.onHover.value()));
            }
            if (this.color != null) {
                textComponent.setColor(net.md_5.bungee.api.ChatColor.getByChar(this.color.getChar()));
            }
            if (this.style != null) {
                final ChatColor[] style = this.style;
                for (int length = style.length, i = 0; i < length; ++i) {
                    switch (style[i]) {
                        case MAGIC: {
                            textComponent.setObfuscated(Boolean.valueOf(true));
                            break;
                        }
                        case BOLD: {
                            textComponent.setBold(Boolean.valueOf(true));
                            break;
                        }
                        case STRIKETHROUGH: {
                            textComponent.setStrikethrough(Boolean.valueOf(true));
                            break;
                        }
                        case UNDERLINE: {
                            textComponent.setUnderlined(Boolean.valueOf(true));
                            break;
                        }
                        case ITALIC: {
                            textComponent.setItalic(Boolean.valueOf(true));
                            break;
                        }
                    }
                }
            }
            return (BaseComponent)textComponent;
        }
        
        public void setStyle(final ChatColor[] style) {
            this.style = style;
        }
        
        public TextBuilder text(final String text) {
            this.text = text;
            return this;
        }
        
        public TextBuilder style(final ChatColor... style) {
            for (int length = style.length, i = 0; i < length; ++i) {
                if (!style[i].isFormat()) {
                    throw new IllegalArgumentException("Argument isn't a style!");
                }
            }
            this.style = style;
            return this;
        }
        
        public static TextBuilder of(final String s) {
            return new TextBuilder().text(s);
        }
        
        public TextBuilder color(final ChatColor color) {
            if (color != null && !color.isColor()) {
                throw new IllegalArgumentException("Argument isn't a color!");
            }
            this.color = color;
            return this;
        }
        
        public TextBuilder onHover(final HoverAction onHover) {
            this.onHover = onHover;
            return this;
        }
    }
}
