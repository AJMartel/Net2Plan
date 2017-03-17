package com.net2plan.gui.launcher;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.net2plan.gui.GUINet2Plan;
import com.net2plan.gui.launcher.utils.GUIRobot;
import com.net2plan.internal.plugins.IGUIModule;
import com.net2plan.internal.plugins.PluginSystem;
import org.apache.commons.cli.*;

import java.awt.*;
import java.awt.event.KeyEvent;

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

            final Option pluginParamOption = new Option("p", "param", true, "Tool/Plugin specific parameters");
            pluginParamOption.setRequired(false);

            options.addOption(pluginOption);
            options.addOption(pluginParamOption);

            parser = new DefaultParser();
            formatter = new HelpFormatter();

            final CommandLine cmd = parser.parse(options, args);
            final String inputPlugin = cmd.getOptionValue("tool");

            // Scan for the given plugin
            ClassLoader cl = GUIPluginLauncher.class.getClassLoader();
            final ImmutableSet<ClassPath.ClassInfo> classesInNet2Plan = ClassPath.from(cl).getTopLevelClassesRecursive("com.net2plan.gui");

            Object plugin = null;
            for (ClassPath.ClassInfo classInfo : classesInNet2Plan)
            {
                if (classInfo.getSimpleName().equalsIgnoreCase(inputPlugin))
                {
                    plugin = Class.forName(classInfo.toString()).newInstance();
                    break;
                }
            }

            if (plugin == null || !(plugin instanceof IGUIModule)) throw new RuntimeException();

            currentPlugin = (IGUIModule) plugin;

            GUINet2Plan.main(args);
            PluginSystem.addPlugin(IGUIModule.class, currentPlugin.getClass());
            PluginSystem.loadExternalPlugins();
            GUINet2Plan.refreshMenu();

            runPlugin();
        } catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(0);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void runPlugin() throws AWTException
    {
        final GUIRobot robot = new GUIRobot();
        robot.setAutoDelay(40);
        robot.setAutoWaitForIdle(true);

        // Showing the tool
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_1);

        robot.delay(1000);

        robot.keyRelease(KeyEvent.VK_1);
        robot.keyRelease(KeyEvent.VK_ALT);

        // Tool specific actions

//        robot.keyPress(KeyEvent.VK_ALT);
//        robot.keyPress(KeyEvent.VK_F4);
//        robot.keyRelease(KeyEvent.VK_F4);
//        robot.keyRelease(KeyEvent.VK_ALT);
//
//        robot.keyPress(KeyEvent.VK_CONTROL);
//        robot.keyPress(KeyEvent.VK_O);
//
//        robot.keyRelease(KeyEvent.VK_O);
//        robot.keyRelease(KeyEvent.VK_CONTROL);
    }


}
