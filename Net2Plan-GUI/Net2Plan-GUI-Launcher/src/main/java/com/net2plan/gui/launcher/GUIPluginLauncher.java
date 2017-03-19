package com.net2plan.gui.launcher;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.net2plan.gui.GUINet2Plan;
import com.net2plan.gui.launcher.utils.GUIRobot;
import com.net2plan.gui.launcher.wrapper.IGUIPluginWrapper;
import com.net2plan.internal.plugins.IGUIModule;
import com.net2plan.internal.plugins.PluginSystem;
import jdk.nashorn.internal.runtime.ParserException;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jorge San Emeterio on 17/03/17.
 */
public class GUIPluginLauncher
{
    private static IGUIModule currentPlugin;

    public static void main(String[] args)
    {
        Options options = null;
        CommandLineParser parser = null;
        HelpFormatter formatter = null;
        try
        {
            // Parse input parameter
            options = new Options();

            final Option pluginOption = new Option("t", "tool", true, "Tool/Plugin to be launched");
            pluginOption.setRequired(true);

            final Option modeParamOption = new Option("m", "mode", true, "Tool/Plugin launch mode");
            pluginOption.setRequired(false);

            final Option pluginParamOption = new Option("p", "param", true, "Tool/Plugin specific parameters");
            pluginParamOption.setRequired(false);
            pluginParamOption.setValueSeparator('=');

            options.addOption(pluginOption);
            options.addOption(modeParamOption);
            options.addOption(pluginParamOption);

            parser = new DefaultParser();
            formatter = new HelpFormatter();

            final CommandLine cmd = parser.parse(options, args);
            final String inputPlugin = cmd.getOptionValue("tool");

            // Scan for the given plugin
            ClassLoader cl = GUIPluginLauncher.class.getClassLoader();
            final ImmutableSet<ClassPath.ClassInfo> classesInNet2Plan = ClassPath.from(cl).getTopLevelClasses("com.net2plan.gui.plugins");

            IGUIModule plugin = null;
            for (ClassPath.ClassInfo classInfo : classesInNet2Plan)
            {
                if (classInfo.getSimpleName().equals(inputPlugin))
                {
                    final Object instance = Class.forName(classInfo.toString()).newInstance();
                    if (instance instanceof IGUIModule)
                    {
                        plugin = (IGUIModule) instance;
                        break;
                    }
                }
            }

            // Plugin not found
            if (plugin == null) throw new ParserException("Plugin: " + inputPlugin + " not found.");
            currentPlugin = plugin;

            // Run Net2Plan
            GUINet2Plan.main(args);
            PluginSystem.addPlugin(IGUIModule.class, currentPlugin.getClass());
            GUINet2Plan.refreshMenu();

            runPlugin();

            // Parse mode and params

            // Looking for plugin wrapper
            final ImmutableSet<ClassPath.ClassInfo> wrappers = ClassPath.from(cl).getTopLevelClasses("com.net2plan.gui.launcher.wrapper");

            IGUIPluginWrapper wrapper = null;
            for (ClassPath.ClassInfo classInfo : wrappers)
            {
                final String className = classInfo.getSimpleName();
                if (className.contains(inputPlugin) && className.contains("Wrapper"))
                {
                    final Object instance = Class.forName(classInfo.toString()).newInstance();
                    if (instance instanceof IGUIPluginWrapper)
                    {
                        wrapper = (IGUIPluginWrapper) instance;
                        break;
                    }
                }
            }

            if (wrapper != null)
            {
                int mode = 1;
                Map<String, String> parameters = new HashMap<>();

                if (cmd.hasOption("mode"))
                {
                    mode = Integer.parseInt(cmd.getOptionValue("mode"));
                    if (cmd.hasOption("param")) parameters = parseParameters(cmd.getOptionValue("param"), pluginParamOption.getValueSeparator());
                }

                wrapper.launchMode(mode, parameters);
            } else
            {
                System.err.println("Debug wrapper not found for class: " + inputPlugin);
            }
        } catch (ParseException e)
        {
            System.err.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        } catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void runPlugin() throws AWTException
    {
        final GUIRobot robot = new GUIRobot();

        // Showing the tool
        final KeyStroke pluginKeyStroke = currentPlugin.getKeyStroke();

        final int keyModifier = getKeyModifier(pluginKeyStroke);

        robot.keyPress(keyModifier);
        robot.keyPress(pluginKeyStroke.getKeyCode());

        robot.delay(1000);

        robot.keyRelease(pluginKeyStroke.getKeyCode());
        robot.keyRelease(keyModifier);
    }

    private static Map<String, String> parseParameters(final String parameter, final char separator)
    {
        return Splitter.on(",").withKeyValueSeparator(separator).split(parameter);
    }

    private static int getKeyModifier(final KeyStroke keyStroke)
    {
        final String modifierString = keyStroke.toString().split("pressed")[0].trim();

        switch (modifierString)
        {
            case "alt":
                return KeyEvent.VK_ALT;
            case "shift":
                return KeyEvent.VK_SHIFT;
            case "ctrl":
                return KeyEvent.VK_CONTROL;
            default:
                return -1;
        }
    }
}
