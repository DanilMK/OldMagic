package net.smok.om.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smok.om.Values;
import net.smok.om.item.*;
import net.smok.om.spell.PlayerCaster;
import net.smok.om.spell.Spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class SpellCommand implements CommandRegistrationCallback {

    public static final MutableComponent ERR_NO_BOOK = Component.translatable("commands.spell.spellBook.no_item").withStyle(ChatFormatting.RED);
    public static final MutableComponent ERR_NO_WAND = Component.translatable("commands.spell.wand.no_item").withStyle(ChatFormatting.RED);
    public static final MutableComponent SUCCESS_REMOVE_BOOK = Component.translatable("commands.spell.spellBook.remove.success_one");
    public static final MutableComponent SUCCESS_REMOVE_WAND = Component.translatable("commands.spell.wand.remove.success_one");
    public static final MutableComponent ERR_NOTHING_TO_REMOVE = Component.translatable("commands.spell.spellBook.remove.nothing_to_remove").withStyle(ChatFormatting.RED);
    public static final MutableComponent ERR_NOTHING_TO_ADD = Component.translatable("commands.spell.spellBook.add.nothing_to_add").withStyle(ChatFormatting.RED);
    public static final MutableComponent SUCCESS_UNLOCK_ALL_SPELL = Component.translatable("commands.spell.player.unlock_all");
    public static final MutableComponent SUCCESS_LOCK_ALL_SPELL = Component.translatable("commands.spell.player.lock_all");

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("spells")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("spellBook")
                .then(Commands.literal("info")
                    .executes(context -> sendBookInfo(context, 0))
                    .then(Commands.argument("page", IntegerArgumentType.integer())
                            .executes(context -> sendBookInfo(context, IntegerArgumentType.getInteger(context, "page")))
                    )
                )
                .then(Commands.literal("add")
                    .then(Commands.argument("spell", ResourceSelectorArgument.resourceSelector(buildContext, Values.SPELLS_KEY))
                            .executes(context -> addSpell(context, ResourceSelectorArgument.getSelectedResources(context, "spell")))
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("index", IntegerArgumentType.integer())
                            .executes(context -> removeSpell(context, IntegerArgumentType.getInteger(context, "index")))
                    ).then(Commands.argument("spell", ResourceSelectorArgument.resourceSelector(buildContext, Values.SPELLS_KEY))
                            .executes(context -> removeSpell(context, ResourceSelectorArgument.getSelectedResources(context, "spell")))
                    )
                )
            ).then(Commands.literal("wand")
                .then(Commands.literal("info")
                    .executes(context -> sendWandInfo(context, 0))
                    .then(Commands.argument("page", IntegerArgumentType.integer())
                            .executes(context -> sendWandInfo(context, IntegerArgumentType.getInteger(context, "page")))
                    )
                ).then(Commands.literal("set")
                    .then(Commands.argument("slot", IntegerArgumentType.integer())
                        .then(Commands.argument("spell", ResourceArgument.resource(buildContext, Values.SPELLS_KEY))
                                .executes(context -> setSpell(context, IntegerArgumentType.getInteger(context, "slot"), ResourceArgument.getResource(context, "spell", Values.SPELLS_KEY)))
                        )
                    ).then(Commands.literal("basic")
                        .then(Commands.argument("spell", ResourceArgument.resource(buildContext, Values.SPELLS_KEY))
                                .executes(context -> setSpell(context, -1, ResourceArgument.getResource(context, "spell", Values.SPELLS_KEY)))
                        )
                    )
                ).then(Commands.literal("remove")
                    .then(Commands.argument("index", IntegerArgumentType.integer())
                            .executes(context -> removeSpellFromWand(context, IntegerArgumentType.getInteger(context, "index")))
                    ).then(Commands.argument("spell", ResourceSelectorArgument.resourceSelector(buildContext, Values.SPELLS_KEY))
                            .executes(context -> removeSpellFromWand(context, ResourceSelectorArgument.getSelectedResources(context, "spell")))
                    )
                )
            ).then(Commands.literal("player")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("unlock")
                            .then(Commands.argument("spell", ResourceArgument.resource(buildContext, Values.SPELLS_KEY))
                                    .executes(commandContext ->
                                            unlockSpell(commandContext, ResourceArgument.getResource(commandContext, "spell", Values.SPELLS_KEY), EntityArgument.getPlayer(commandContext, "player")))
                            )
                    ).then(Commands.literal("unlockAll")
                            .executes(commandContext -> unlockAllSpells(commandContext, EntityArgument.getPlayer(commandContext, "player")))
                    ).then(Commands.literal("lock")
                            .then(Commands.argument("spell", ResourceArgument.resource(buildContext, Values.SPELLS_KEY))
                                    .executes(commandContext ->
                                            lockSpell(commandContext, ResourceArgument.getResource(commandContext, "spell", Values.SPELLS_KEY), EntityArgument.getPlayer(commandContext, "player")))
                            )
                    ).then(Commands.literal("lockAll")
                            .executes(commandContext -> lockAllSpells(commandContext, EntityArgument.getPlayer(commandContext, "player")))
                    )
                )
            )
        );
    }

    private int unlockSpell(CommandContext<CommandSourceStack> context, Holder.Reference<Spell> spell, Player player) {
        if (player instanceof PlayerCaster caster) {
            caster.oldMagic$getCastHolder().unlockSpell(spell);
            sendFeedback(context, () -> Component.translatable("commands.spell.player.unlock", spellInfo(spell)));
            return 1;
        }
        return 0;
    }

    private int lockSpell(CommandContext<CommandSourceStack> context, Holder.Reference<Spell> spell, Player player) {
        if (player instanceof PlayerCaster caster) {
            caster.oldMagic$getCastHolder().lockSpell(spell);
            sendFeedback(context, () -> Component.translatable("commands.spell.player.lock", spellInfo(spell)));
            return 1;
        }
        return 0;
    }

    private int unlockAllSpells(CommandContext<CommandSourceStack> context, Player player) {
        if (player instanceof PlayerCaster caster) {
            caster.oldMagic$getCastHolder().unlockSpells(context.getSource().getLevel().holderLookup(Values.SPELLS_KEY).listElements().toList());
            sendFeedback(context, () -> SUCCESS_UNLOCK_ALL_SPELL);
            return 1;
        }
        return 0;
    }

    private int lockAllSpells(CommandContext<CommandSourceStack> context, Player player) {
        if (player instanceof PlayerCaster caster) {
            caster.oldMagic$getCastHolder().lockAllSpells();
            sendFeedback(context, () -> SUCCESS_LOCK_ALL_SPELL);
            return 1;
        }
        return 0;
    }


    private int removeSpell(CommandContext<CommandSourceStack> context, int index) {
        BookHolder book = getBook(context);
        if (book == null) return 0;

        if (index < -1 || index >= book.spellStacks.size()) {
            sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.remove.wrong_index", index, book.spellStacks.size()).withStyle(ChatFormatting.RED));
            return 0;
        }

        book.spellStacks.remove(index);
        book.itemStack.set(OMDataComponents.BOOK_CONTENTS, new BookContents(book.spellStacks, book.selectedSpell));
        sendFeedback(context, () -> SUCCESS_REMOVE_BOOK);


        return 1;
    }

    private int removeSpell(CommandContext<CommandSourceStack> context, Collection<Holder.Reference<Spell>> spells) {
        BookHolder book = getBook(context);
        if (book == null) return 0;

        if (spells.isEmpty()) {
            sendFeedback(context, () -> ERR_NOTHING_TO_REMOVE);
            return 0;
        }

        AtomicInteger count = new AtomicInteger();
        spells.forEach(spellReference -> {
            if (book.spellStacks.removeIf(spellStack -> spellStack.scroll().spell().is(spellReference))) {
                count.getAndIncrement();
            }
        });

        if (count.get() > 0) {
            book.itemStack.set(OMDataComponents.BOOK_CONTENTS, new BookContents(book.spellStacks, book.selectedSpell));
            sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.remove.success_more", count));
            return 1;
        } else {
            sendFeedback(context, () -> ERR_NOTHING_TO_REMOVE);
            return 0;
        }
    }

    private int addSpell(CommandContext<CommandSourceStack> context, Collection<Holder.Reference<Spell>> spells) {
        BookHolder book = getBook(context);
        if (book == null) {
            return 0;
        }

        if (spells.isEmpty()) {
            sendFeedback(context, () -> ERR_NOTHING_TO_ADD);
            return 0;
        }

        spells.forEach(spellReference -> book.spellStacks.add(new BookContents.ScrollStack(new Scroll(spellReference), 1)));
        book.itemStack.set(OMDataComponents.BOOK_CONTENTS, new BookContents(book.spellStacks, book.selectedSpell));
        sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.add.success_more", spells.size()));

        return 1;
    }

    private int sendBookInfo(CommandContext<CommandSourceStack> context, int page) {
        BookHolder book = getBook(context);
        if (book == null) return 0;

        sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.info"));

        if (book.spellStacks.isEmpty())
            sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.info.empty"));
        else {
            if (book.selectedSpell < 0 || book.selectedSpell >= book.spellStacks.size())
                sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.info.selected_empty"));
            else sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.info.selected").append(spellInfo(book.spellStacks.get(book.selectedSpell).scroll().spell())));


            int p = Math.min(page, (book.spellStacks.size() - 1) / 5);
            for (int i = p * 5; i < book.spellStacks.size(); i++) {
                Holder<Spell> spellStack = book.spellStacks.get(i).scroll().spell();
                sendFeedback(context, () -> spellInfo(spellStack));
            }

            if (book.spellStacks.size() > 5) {
                context.getSource().sendSuccess(() -> {
                    MutableComponent foots = Component.translatable("commands.spell.spellBook.info.page_suggest.0");
                    foots.append(Component.literal("/spells spellBook info <page>").withStyle(ChatFormatting.UNDERLINE));
                    foots.append(Component.translatable("commands.spell.spellBook.info.page_suggest.1"));
                    return foots;
                }, false);
            }
        }

        return 1;
    }

    private int sendWandInfo(CommandContext<CommandSourceStack> context, int page) {

        WandHolder wand = getWand(context);
        if (wand == null) return 0;

        sendFeedback(context, () -> Component.translatable("commands.spell.wand.info"));


        if (wand.contents.size() == 0)
            sendFeedback(context, () -> Component.translatable("commands.spell.wand.info.empty"));
        else {

            int p = Math.min(page, (wand.contents.size() - 1) / 5);
            for (int i = p * 5; i < wand.contents.size() || i < wand.contents.size(); i++) {
                Holder<Spell> spellStack = wand.contents.getSpell(i);
                sendFeedback(context, () -> spellInfo(spellStack));
            }

            if (wand.contents.size() > 5) {
                context.getSource().sendSuccess(() -> {
                    MutableComponent foots = Component.translatable("commands.spell.spellBook.info.page_suggest.0");
                    foots.append(Component.literal("/spells wand info <page>").withStyle(ChatFormatting.UNDERLINE));
                    foots.append(Component.translatable("commands.spell.spellBook.info.page_suggest.1"));
                    return foots;
                }, false);
            }
        }

        return 1;
    }


    private int setSpell(CommandContext<CommandSourceStack> context, int slot, Holder.Reference<Spell> spell) {
        WandHolder wand = getWand(context);
        if (wand == null) return 0;

        if (slot < -1 || slot >= wand.contents.size()) {
            sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.remove.wrong_index", slot, wand.contents.size()).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (slot == -1) {
            wand.itemStack.set(OMDataComponents.SPELL_HOLDER,  spell);
        } else {
            wand.contents.setSpell(slot, spell);
            wand.itemStack.set(OMDataComponents.WAND_CONTENTS, wand.contents.toImmutable());
        }

        sendFeedback(context, () -> Component.translatable("commands.spell.wand.set.success", spellInfo(spell).getString(), slot == -1 ? "basic" : slot));

        return 1;
    }

    private int removeSpellFromWand(CommandContext<CommandSourceStack> context, int index) {
        WandHolder wand = getWand(context);
        if (wand == null) return 0;

        if (index < 0 || index >= wand.contents.size()) {
            sendFeedback(context, () -> Component.translatable("commands.spell.spellBook.remove.wrong_index", index, wand.contents.size()).withStyle(ChatFormatting.RED));
            return 0;
        }

        wand.contents.setSpell(index, null);
        wand.itemStack.set(OMDataComponents.WAND_CONTENTS, wand.contents.toImmutable());
        sendFeedback(context, () -> SUCCESS_REMOVE_WAND);


        return 1;
    }

    private int removeSpellFromWand(CommandContext<CommandSourceStack> context, Collection<Holder.Reference<Spell>> spells) {
        WandHolder wand = getWand(context);
        if (wand == null) return 0;

        if (spells.isEmpty()) {
            sendFeedback(context, () -> ERR_NOTHING_TO_REMOVE);
            return 0;
        }

        AtomicInteger count = new AtomicInteger();
        spells.forEach(spellReference -> {
            count.addAndGet(wand.contents.removeIf(spellSlot -> spellSlot.spell().is(spellReference)));
        });

        if (count.get() > 0) {
            wand.itemStack.set(OMDataComponents.WAND_CONTENTS, wand.contents.toImmutable());
            sendFeedback(context, () -> Component.translatable("commands.spell.wand.remove.success_more", count));
            return 1;
        } else {
            sendFeedback(context, () -> ERR_NOTHING_TO_REMOVE);
            return 0;
        }
    }


    private static void sendFeedback(CommandContext<CommandSourceStack> context, Supplier<Component> feedback) {
        context.getSource().sendSuccess(feedback, false);
    }

    private Component spellInfo(Holder<Spell> spellStack) {
        if (spellStack == null)
            return Component.literal("[]");
        return Component.literal("[").append(Component.translatable(Spell.languageKey(spellStack))).append("]");
    }

    private BookHolder getBook(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ItemStack selectedItem = Objects.requireNonNull(context.getSource().getPlayer()).getInventory().getSelectedItem();
            BookContents bookContents = selectedItem.get(OMDataComponents.BOOK_CONTENTS);
            if (bookContents != null) {
                return new BookHolder(selectedItem, new ArrayList<>(bookContents.scrolls()), bookContents.selected());
            }
        }

        sendFeedback(context, () -> ERR_NO_BOOK);
        return null;
    }

    private WandHolder getWand(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ItemStack selectedItem = Objects.requireNonNull(context.getSource().getPlayer()).getInventory().getSelectedItem();
            WandContents wandContents = selectedItem.get(OMDataComponents.WAND_CONTENTS);
            if (wandContents != null) {
                return new WandHolder(selectedItem, wandContents.mutable());
            }

        }

        sendFeedback(context, () -> ERR_NO_WAND);
        return null;
    }

    private record BookHolder(ItemStack itemStack, ArrayList<BookContents.ScrollStack> spellStacks, int selectedSpell) {}

    private record WandHolder(ItemStack itemStack, WandContents.Mutable contents) {}
}
