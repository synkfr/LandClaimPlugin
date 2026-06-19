package org.ayosynk.landClaimPlugin.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Reflection-based helper for sending Geyser forms to Bedrock players.
 *
 * <p>Geyser 2.x exposes its form API under {@code org.geysermc.geyser.api.*}. We resolve the
 * classes lazily on first use so this plugin does <em>not</em> need a hard runtime dependency
 * on Geyser — it will simply report {@link #isAvailable()} as {@code false} when Geyser is
 * not installed, and callers should fall back to their Java-only path.</p>
 *
 * <p>Supported forms:</p>
 * <ul>
 *   <li>{@link #sendCustomFormTextInput} — text field on a native Bedrock form
 *       (replaces the chat-prompt fallback used by {@code AnvilInputGUI}).</li>
 *   <li>{@link #sendSimpleForm} — buttons-only form for menu-style choices.</li>
 *   <li>{@link #sendModalForm} — two-button (yes/no) confirmation dialog.</li>
 * </ul>
 */
public final class GeyserFormHelper {

    private static boolean initialized = false;
    private static boolean available = false;

    // Cached reflection handles for Geyser 2.x API.
    private static Object geyserApiInstance = null;
    private static Class<?> formClass = null;
    private static Class<?> simpleFormClass = null;
    private static Class<?> customFormClass = null;
    private static Class<?> modalFormClass = null;

    private GeyserFormHelper() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;

        // Try Geyser 2.x API first.
        try {
            Class<?> apiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            Method apiMethod = apiClass.getMethod("api");
            geyserApiInstance = apiMethod.invoke(null);
            if (geyserApiInstance == null) {
                return;
            }

            formClass = tryLoad("org.geysermc.geyser.api.form.Form");
            simpleFormClass = tryLoad("org.geysermc.geyser.api.form.SimpleForm");
            customFormClass = tryLoad("org.geysermc.geyser.api.form.CustomForm");
            modalFormClass = tryLoad("org.geysermc.geyser.api.form.ModalForm");

            // Require at least one form type to consider the API usable.
            available = simpleFormClass != null || customFormClass != null || modalFormClass != null;
        } catch (Throwable ignored) {
            // Geyser 2.x not available.
        }

        // Fall back to Geyser-Spigot 1.x if 2.x didn't resolve. Same class names, but
        // loaded through the Spigot plugin classloader to avoid linkage issues.
        if (!available) {
            try {
                Class<?> spigotApi = Class.forName("org.geysermc.geyser.spigot.GeyserSpigotPlugin");
                Method getInstance = spigotApi.getMethod("getInstance");
                geyserApiInstance = getInstance.invoke(null);
                if (geyserApiInstance == null) {
                    return;
                }

                formClass = tryLoad("org.geysermc.geyser.spigot.form.Form");
                simpleFormClass = tryLoad("org.geysermc.geyser.spigot.form.SimpleForm");
                customFormClass = tryLoad("org.geysermc.geyser.spigot.form.CustomForm");
                modalFormClass = tryLoad("org.geysermc.geyser.spigot.form.ModalForm");

                available = simpleFormClass != null || customFormClass != null || modalFormClass != null;
            } catch (Throwable ignored) {
                // Geyser not installed or form API not available in this version.
            }
        }
    }

    private static Class<?> tryLoad(String fqn) {
        try {
            return Class.forName(fqn);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @return {@code true} if a Geyser form API was successfully resolved at runtime.
     */
    public static boolean isAvailable() {
        init();
        return available;
    }

    /**
     * Convenience: {@code true} when both Geyser forms are available and the player is
     * a Bedrock player. Callers should use this to decide whether to take the form path
     * or the Java-only path.
     */
    public static boolean shouldUseForms(Player player) {
        return isAvailable() && GeyserHelper.isBedrockPlayer(player);
    }

    // ---------------------------------------------------------------------
    // CustomForm (text input)
    // ---------------------------------------------------------------------

    /**
     * Send a CustomForm with a single text input field. The response is delivered to
     * {@code onResponse} with the entered text, or {@code null} if the player closed
     * the form.
     *
     * <p>If Geyser forms are not available, {@code onResponse} is invoked synchronously
     * with {@code null}.</p>
     */
    public static void sendCustomFormTextInput(Player player, String title, String content,
                                                String label, String defaultText,
                                                Consumer<String> onResponse) {
        if (!isAvailable() || customFormClass == null) {
            safeAccept(onResponse, null);
            return;
        }
        if (player == null || !player.isOnline()) {
            safeAccept(onResponse, null);
            return;
        }

        try {
            Object builder = customFormClass.getMethod("builder").invoke(null);

            invokeIfPresent(builder, "title", String.class, title);
            invokeIfPresent(builder, "content", String.class, content);
            invokeIfPresent(builder, "label", String.class, title);

            // CustomForm.Builder.input(String label, String defaultValue) (2.x) — may
            // also be input(String, String, String) with a placeholder. Try the
            // 2-arg variant first, fall back to 3-arg if needed.
            Method inputMethod = findMethod(builder.getClass(), "input", String.class, String.class);
            if (inputMethod == null) {
                inputMethod = findMethod(builder.getClass(), "input", String.class, String.class, String.class);
                if (inputMethod != null) {
                    inputMethod.invoke(builder, label, defaultText != null ? defaultText : "", "");
                }
            } else {
                inputMethod.invoke(builder, label, defaultText != null ? defaultText : "");
            }

            // Set response handler. CustomFormResponse has input(int index) or input(String label).
            Class<?> responseClass = tryLoad("org.geysermc.geyser.api.form.response.CustomFormResponse");
            if (responseClass == null) {
                responseClass = tryLoad("org.geysermc.geyser.spigot.form.response.CustomFormResponse");
            }
            final Class<?> finalResponseClass = responseClass;
            Consumer<Object> responseHandler = response -> {
                try {
                    Object value = null;
                    if (finalResponseClass != null) {
                        Method byIndex = findMethod(finalResponseClass, "input", int.class);
                        if (byIndex != null) {
                            value = byIndex.invoke(response, 0);
                        } else {
                            Method byLabel = findMethod(finalResponseClass, "input", String.class);
                            if (byLabel != null) {
                                value = byLabel.invoke(response, label);
                            }
                        }
                    }
                    safeAccept(onResponse, value == null ? null : value.toString());
                } catch (Throwable t) {
                    safeAccept(onResponse, null);
                }
            };
            invokeIfPresent(builder, "validResultHandler", Consumer.class, responseHandler);
            invokeIfPresent(builder, "closedHandler", Runnable.class, (Runnable) () -> safeAccept(onResponse, null));

            Object form = builder.getClass().getMethod("build").invoke(builder);

            // Send via Form#send(Player) (2.x) or via the API instance (older).
            Method sendMethod = findMethod(formClass, "send", Player.class);
            if (sendMethod != null) {
                sendMethod.invoke(form, player);
            } else {
                Method apiSend = findMethod(geyserApiInstance.getClass(), "sendForm", Player.class, formClass);
                if (apiSend != null) {
                    apiSend.invoke(geyserApiInstance, player, form);
                } else {
                    safeAccept(onResponse, null);
                }
            }
        } catch (Throwable t) {
            safeAccept(onResponse, null);
        }
    }

    // ---------------------------------------------------------------------
    // SimpleForm (buttons)
    // ---------------------------------------------------------------------

    /**
     * Send a SimpleForm with a list of buttons. {@code onResponse} is called with the
     * index of the clicked button, or {@code -1} if the form was closed.
     */
    public static void sendSimpleForm(Player player, String title, String content,
                                       List<String> buttonLabels, IntConsumer onResponse) {
        if (!isAvailable() || simpleFormClass == null) {
            safeAcceptInt(onResponse, -1);
            return;
        }
        if (player == null || !player.isOnline()) {
            safeAcceptInt(onResponse, -1);
            return;
        }

        try {
            Object builder = simpleFormClass.getMethod("builder").invoke(null);
            invokeIfPresent(builder, "title", String.class, title);
            invokeIfPresent(builder, "content", String.class, content);

            // Add buttons. The 2.x API uses button(String label). Some forks accept
            // button(String label, String url/icon) — we ignore the extra parameter.
            for (String label : buttonLabels) {
                Method buttonMethod = findMethod(builder.getClass(), "button", String.class);
                if (buttonMethod != null) {
                    buttonMethod.invoke(builder, label);
                }
            }

            Class<?> responseClass = tryLoad("org.geysermc.geyser.api.form.response.SimpleFormResponse");
            if (responseClass == null) {
                responseClass = tryLoad("org.geysermc.geyser.spigot.form.response.SimpleFormResponse");
            }
            final Class<?> finalResponseClass = responseClass;
            Consumer<Object> responseHandler = response -> {
                try {
                    int clicked = -1;
                    if (finalResponseClass != null) {
                        Method getter = findMethod(finalResponseClass, "clickedButtonId");
                        if (getter == null) {
                            getter = findMethod(finalResponseClass, "clickedButton");
                        }
                        if (getter != null) {
                            Object result = getter.invoke(response);
                            if (result instanceof Number) {
                                clicked = ((Number) result).intValue();
                            }
                        }
                    }
                    safeAcceptInt(onResponse, clicked);
                } catch (Throwable t) {
                    safeAcceptInt(onResponse, -1);
                }
            };
            invokeIfPresent(builder, "validResultHandler", Consumer.class, responseHandler);
            invokeIfPresent(builder, "closedHandler", Runnable.class, (Runnable) () -> safeAcceptInt(onResponse, -1));

            Object form = builder.getClass().getMethod("build").invoke(builder);
            Method sendMethod = findMethod(formClass, "send", Player.class);
            if (sendMethod != null) {
                sendMethod.invoke(form, player);
            } else {
                Method apiSend = findMethod(geyserApiInstance.getClass(), "sendForm", Player.class, formClass);
                if (apiSend != null) {
                    apiSend.invoke(geyserApiInstance, player, form);
                } else {
                    safeAcceptInt(onResponse, -1);
                }
            }
        } catch (Throwable t) {
            safeAcceptInt(onResponse, -1);
        }
    }

    // ---------------------------------------------------------------------
    // ModalForm (yes/no)
    // ---------------------------------------------------------------------

    /**
     * Send a ModalForm confirmation dialog with two buttons. {@code onResponse} is called
     * with {@code true} if the player confirmed, {@code false} if they denied, or
     * {@code false} if the form was closed.
     */
    public static void sendModalForm(Player player, String title, String content,
                                      String confirmText, String denyText,
                                      Consumer<Boolean> onResponse) {
        if (!isAvailable() || modalFormClass == null) {
            safeAccept(onResponse, Boolean.FALSE);
            return;
        }
        if (player == null || !player.isOnline()) {
            safeAccept(onResponse, Boolean.FALSE);
            return;
        }

        try {
            Object builder = modalFormClass.getMethod("builder").invoke(null);
            invokeIfPresent(builder, "title", String.class, title);
            invokeIfPresent(builder, "content", String.class, content);

            // 2.x uses confirmButton(String) and denyButton(String). Some forks use
            // button1/button2 — we try both naming conventions.
            invokeIfPresent(builder, "confirmButton", String.class, confirmText);
            invokeIfPresent(builder, "denyButton", String.class, denyText);
            invokeIfPresent(builder, "button1", String.class, confirmText);
            invokeIfPresent(builder, "button2", String.class, denyText);

            Class<?> responseClass = tryLoad("org.geysermc.geyser.api.form.response.ModalFormResponse");
            if (responseClass == null) {
                responseClass = tryLoad("org.geysermc.geyser.spigot.form.response.ModalFormResponse");
            }
            final Class<?> finalResponseClass = responseClass;
            Consumer<Object> responseHandler = response -> {
                try {
                    boolean confirmed = false;
                    if (finalResponseClass != null) {
                        Method getter = findMethod(finalResponseClass, "confirmed");
                        if (getter == null) {
                            getter = findMethod(finalResponseClass, "result");
                        }
                        if (getter != null) {
                            Object result = getter.invoke(response);
                            if (result instanceof Boolean) {
                                confirmed = (Boolean) result;
                            }
                        }
                    }
                    safeAccept(onResponse, confirmed);
                } catch (Throwable t) {
                    safeAccept(onResponse, Boolean.FALSE);
                }
            };
            invokeIfPresent(builder, "validResultHandler", Consumer.class, responseHandler);
            invokeIfPresent(builder, "closedHandler", Runnable.class, (Runnable) () -> safeAccept(onResponse, Boolean.FALSE));

            Object form = builder.getClass().getMethod("build").invoke(builder);
            Method sendMethod = findMethod(formClass, "send", Player.class);
            if (sendMethod != null) {
                sendMethod.invoke(form, player);
            } else {
                Method apiSend = findMethod(geyserApiInstance.getClass(), "sendForm", Player.class, formClass);
                if (apiSend != null) {
                    apiSend.invoke(geyserApiInstance, player, form);
                } else {
                    safeAccept(onResponse, Boolean.FALSE);
                }
            }
        } catch (Throwable t) {
            safeAccept(onResponse, Boolean.FALSE);
        }
    }

    // ---------------------------------------------------------------------
    // Reflection helpers
    // ---------------------------------------------------------------------

    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            // Try walking superclasses / interfaces.
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(name)) continue;
                Class<?>[] existing = m.getParameterTypes();
                if (existing.length != paramTypes.length) continue;
                boolean match = true;
                for (int i = 0; i < existing.length; i++) {
                    if (!existing[i].isAssignableFrom(paramTypes[i]) && !paramTypes[i].isAssignableFrom(existing[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) return m;
            }
            return null;
        }
    }

    private static void invokeIfPresent(Object target, String methodName, Class<?> paramType, Object value) {
        Method m = findMethod(target.getClass(), methodName, paramType);
        if (m != null) {
            try {
                m.invoke(target, value);
            } catch (Throwable ignored) {
                // Method exists but failed — skip silently.
            }
        }
    }

    private static <T> void safeAccept(Consumer<T> consumer, T value) {
        if (consumer == null) return;
        try {
            consumer.accept(value);
        } catch (Throwable ignored) {
            // Never let a callback exception break the form flow.
        }
    }

    private static void safeAcceptInt(IntConsumer consumer, int value) {
        if (consumer == null) return;
        try {
            consumer.accept(value);
        } catch (Throwable ignored) {
            // Never let a callback exception break the form flow.
        }
    }
}
