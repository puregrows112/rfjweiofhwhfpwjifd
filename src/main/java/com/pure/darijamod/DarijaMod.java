package com.pure.darijamod;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("darijamod")
public class DarijaMod {

    // ---- الجمل بالدارجة ----
    private static final String[] GREETINGS = {
        "واو مشتري جديد، نشوفو أش بغا!",
        "السلام عليكم، أش بغيتي اليوم؟",
        "مرحبا بيك، عندي سلعة مزيانة!",
        "أهلا صاحبي، قرب قرب!",
        "لباس عليك؟ شنو بغيتي نعاونك؟",
        "غادي نعطيك ثمن مزيان، والله!",
        "هاه، جيتي فوقتك، كاين شي حاجة جديدة!"
    };
    private static final String[] HURT_LIGHT = {
        "ريح!", "أودي شنو كتدير؟", "عيب عليك!", "وا حشومة عليك!"
    };
    private static final String[] HURT_MEDIUM = {
        "صافي ريح!", "بزاف عليك، وقف!", "الله يهديك، شنو دارت ليك؟", "آه آه، كفاك!"
    };
    private static final String[] HURT_HEAVY = {
        "صافي راه قويتي! واش غتريح فحالك؟",
        "آه يا ويلي، بغيتي تقتلني؟",
        "الله ياخذ الحق، وقف!"
    };
    private static final String[] DEATH = {
        "تفو عليك!", "الله ياخذ فيك الحق...", "آه... صافي مشيت..."
    };

    private final Map<UUID, Long> lastSpoke = new HashMap<>();

    // Forge 1.21.11 (EventBus 7): the constructor receives the loading context
    public DarijaMod(FMLJavaModLoadingContext context) {
        BusGroup.DEFAULT.register(MethodHandles.lookup(), this);
    }

    private static String pick(String[] arr, Villager v) {
        return arr[v.getRandom().nextInt(arr.length)];
    }

    /** الفيلجر كيهدر: الصوت ديالو الأصلي + كيلتفت للاعب + الرسالة بالدارجة */
    private void speak(Villager villager, Player player, String text, SoundEvent sound, boolean shakeHead) {
        // صوت الفيلجر الأصلي (بنبرة مختلفة شوية كل مرة باش يبان طبيعي)
        float pitch = 0.9f + villager.getRandom().nextFloat() * 0.25f;
        villager.playSound(sound, 1.0f, pitch);
        // كيلتفت بوجهو نحو اللاعب
        villager.getLookControl().setLookAt(player, 30.0f, 30.0f);
        // أنيميشن ديال الراس (الفيلجر كيهز راسو) فاش كيكون غاضب
        if (shakeHead) {
            villager.setUnhappyCounter(40);
        }
        player.displayClientMessage(Component.literal("فيلجر: " + text), false);
    }

    private boolean canSpeak(Villager villager, long cooldownTicks) {
        long now = villager.level().getGameTime();
        Long last = lastSpoke.get(villager.getUUID());
        if (last != null && now - last < cooldownTicks) return false;
        lastSpoke.put(villager.getUUID(), now);
        return true;
    }

    @SubscribeEvent
    public void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getTarget() instanceof Villager villager) {
            Player player = event.getEntity();
            if (!player.level().isClientSide() && canSpeak(villager, 40)) {
                speak(villager, player, pick(GREETINGS, villager), SoundEvents.VILLAGER_YES, false);
            }
        }
    }

    @SubscribeEvent
    public void onVillagerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Villager villager
                && event.getSource().getEntity() instanceof Player player
                && !player.level().isClientSide()
                && canSpeak(villager, 20)) {
            float health = villager.getHealth();
            String[] lines = health > 15 ? HURT_LIGHT : (health > 8 ? HURT_MEDIUM : HURT_HEAVY);
            speak(villager, player, pick(lines, villager), SoundEvents.VILLAGER_NO, true);
        }
    }

    @SubscribeEvent
    public void onVillagerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Villager villager
                && event.getSource().getEntity() instanceof Player player
                && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("فيلجر: " + pick(DEATH, villager)), false);
        }
    }
}
