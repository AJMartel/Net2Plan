package com.net2plan.gui.utils;

import com.jom.OptimizationProblem.JOMSolver;
import com.jom.SolverTester;
import com.net2plan.interfaces.networkDesign.Configuration;
import com.net2plan.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jorge San Emeterio on 2/03/17.
 */
public class SolverCheckPanel extends JPanel implements ActionListener
{
    private final JToolBar tb_buttons;
    private final List<JButton> btn_solverButtons;
    private final JButton btn_checkAll;
    private final JTextArea txt_info;

    private final JPanel pn_saveConfirm;
    private final JButton btn_accept, btn_refuse;

    private enum OS
    {
        windows, linux, macintosh, unknown
    }

    private final String NEW_LINE = "\n";
    private final String MESSAGE_HEADER = "MESSAGE: ";
    private final String WARNING_HEADER = "WARNING: ";
    private final String ERROR_HEADER = "ERROR: ";

    private OS currentOS;

    private boolean isJNAPathSet;
    private boolean isJAVAPathSet;

    private String JNAPath;
    private String JAVAPath;

    public SolverCheckPanel()
    {
        super();

        this.setLayout(new BorderLayout());

        this.tb_buttons = new JToolBar(JToolBar.VERTICAL);
        this.tb_buttons.setFloatable(false);
        this.tb_buttons.setFocusable(false);
        this.tb_buttons.setBorderPainted(false);
        this.tb_buttons.setRollover(false);

        this.btn_solverButtons = new ArrayList<>();

        // Adding as many buttons as solvers there are.
        for (JOMSolver solver : JOMSolver.values())
        {
            final JButton btn = new JButton("Check " + solver.name());
            btn.setFocusable(false);
            btn.addActionListener(this);

            btn_solverButtons.add(btn);
            tb_buttons.add(btn);
        }

        // Add check all
        this.btn_checkAll = new JButton("Check all");
        this.btn_checkAll.setFocusable(false);
        this.btn_checkAll.addActionListener(this);
        this.tb_buttons.add(btn_checkAll);

        this.txt_info = new JTextArea();
        this.txt_info.setText("");

        // Build confirm dialog
        this.pn_saveConfirm = new JPanel(new BorderLayout());
        this.pn_saveConfirm.setVisible(false);
        this.btn_accept = new JButton("Save");
        this.btn_accept.setFocusable(false);
        this.btn_refuse = new JButton("Cancel");
        this.btn_refuse.setFocusable(false);

        this.pn_saveConfirm.add(new JLabel("New solver path has been found. Save it under configuration?: "), BorderLayout.CENTER);

        final JPanel aux = new JPanel(new GridBagLayout());
        aux.add(btn_accept);
        aux.add(btn_refuse);

        this.pn_saveConfirm.add(aux, BorderLayout.EAST);

        final JPanel pn_text = new JPanel(new BorderLayout());

        pn_text.add(new JScrollPane(txt_info), BorderLayout.CENTER);
        pn_text.add(pn_saveConfirm, BorderLayout.SOUTH);

        this.add(tb_buttons, BorderLayout.EAST);
        this.add(pn_text, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
            // Previous steps

            // Clean window
            txt_info.setText("");

            // Do not allow to click again
            tb_buttons.setEnabled(false);

            // Getting OS
            final Pair<OS, String> foundOS = getOS();
            final OS currentOS = foundOS.getFirst();
            final String OSName = foundOS.getSecond();

            txt_info.append(MESSAGE_HEADER + "Checking for current operating system..." + NEW_LINE);

            switch (currentOS)
            {
                case windows:
                    txt_info.append(MESSAGE_HEADER + "Found Windows operating system: " + OSName + NEW_LINE);
                    break;
                case linux:
                    txt_info.append(MESSAGE_HEADER + "Found Linux operating system: " + OSName + NEW_LINE);
                    break;
                case macintosh:
                    txt_info.append(MESSAGE_HEADER + "Found Macintosh operating system: " + OSName + NEW_LINE);
                    break;
                case unknown:
                default:
                    txt_info.append(ERROR_HEADER + "Found an unknown operating system." + NEW_LINE);
                    txt_info.append(ERROR_HEADER + "The tester cannot continue without knowing the operating system it is working on." + NEW_LINE);
                    throw new RuntimeException("Unknown operating system: " + OSName);
            }

            this.currentOS = currentOS;

            txt_info.append(NEW_LINE);

            txt_info.append(MESSAGE_HEADER + "Checking current runtime environment..." + NEW_LINE);

            final String jnaDefaultPath = System.getProperty("jna.library.path");

            if (jnaDefaultPath != null)
            {
                txt_info.append(MESSAGE_HEADER + "Default JNA library path set to: " + jnaDefaultPath + NEW_LINE);
                isJNAPathSet = true;
                JNAPath = jnaDefaultPath;
            } else
            {
                txt_info.append(WARNING_HEADER + "Default JNA library path is not currently defined..." + NEW_LINE);
                isJNAPathSet = false;
                JNAPath = null;
            }

            final String javaDefaultPath = System.getProperty("java.library.path");

            if (javaDefaultPath != null)
            {
                txt_info.append(MESSAGE_HEADER + "Default JAVA library path set to: " + javaDefaultPath + NEW_LINE);
                isJAVAPathSet = true;
                JAVAPath = javaDefaultPath;
            } else
            {
                txt_info.append(WARNING_HEADER + "Default JAVA library path is not currently defined..." + NEW_LINE);
                isJAVAPathSet = false;
                JAVAPath = null;
            }

            // Calculating selected solver
            final List<JOMSolver> selectedSolvers = new ArrayList<>();

            final JButton src = (JButton) actionEvent.getSource();
            final String selectedSolverName = src.getText().replace("Check ", "").trim();

            try
            {
                final JOMSolver selectedSolver = JOMSolver.valueOf(selectedSolverName);
                selectedSolvers.add(selectedSolver);
            } catch (IllegalArgumentException e)
            {
                selectedSolvers.addAll(Arrays.asList(JOMSolver.values()));
            }

            if (selectedSolvers.isEmpty())
            {
                txt_info.append(ERROR_HEADER + "Internal problem: no solver was selected for testing." + NEW_LINE);
                throw new RuntimeException("Could not find a solver for testing. Meaning that the provided solver is unknown or built poorly.");
            }

            txt_info.append(NEW_LINE);

            txt_info.append(MESSAGE_HEADER + "Checking for solvers: " + Arrays.toString(selectedSolvers.toArray()) + NEW_LINE);

            txt_info.append(NEW_LINE);

            // Checking solvers
            for (JOMSolver solvers : selectedSolvers)
            {
                switch (solvers)
                {
                    case glpk:
                        checkForSolver(JOMSolver.glpk);
                        break;
                    case ipopt:
                        checkForSolver(JOMSolver.ipopt);
                        break;
                    case cplex:
                        checkForSolver(JOMSolver.cplex);
                        break;
                    case xpress:
                        checkForSolver(JOMSolver.xpress);
                        break;
                    default:
                        txt_info.append(ERROR_HEADER + "Unknown solver has been provided: " + solvers.name() + NEW_LINE);
                        txt_info.append(ERROR_HEADER + "The tester is trying to work with unknown solvers and cannot continue." + NEW_LINE);
                        throw new RuntimeException("Unknown solver was provided: " + solvers.name());
                }
            }

        } catch (Exception ex)
        {
            txt_info.append(ERROR_HEADER + "An error has been found while running the solver tester..." + NEW_LINE);
            txt_info.append(ERROR_HEADER + "Check the console for more information." + NEW_LINE);
            txt_info.append(ERROR_HEADER + "Tester shutting down..." + NEW_LINE);
            ex.printStackTrace();
        } finally
        {
            tb_buttons.setEnabled(true);
        }
    }

    private void checkForSolver(JOMSolver solver)
    {
        final String solverPath;
        final String solverName = solver.name();
        final String solverNameUppercase = solverName.toUpperCase();

        String message;

        txt_info.append(MESSAGE_HEADER + "Looking for solver: " + solverNameUppercase + NEW_LINE);
        solverPath = Configuration.getDefaultSolverLibraryName(solverName);

        final boolean useDefaultPath = solverPath.isEmpty();

        if (useDefaultPath)
        {
            txt_info.append(WARNING_HEADER + "Directory for " + solverNameUppercase + " solver has been left blank. Using default path..." + NEW_LINE);
            checkSolverAtDefaultFolder(solver);
        } else
        {
            message = callJOM(solver, solverPath);

            if (message.isEmpty())
            {
                txt_info.append(MESSAGE_HEADER + "Solver " + solverNameUppercase + " has been found at directory: " + solverPath + NEW_LINE);
            } else
            {
                txt_info.append(WARNING_HEADER + "Solver " + solverNameUppercase + " could not be found at directory: " + solverPath + NEW_LINE);

                txt_info.append(MESSAGE_HEADER + "Retrying..." + NEW_LINE);
                txt_info.append(MESSAGE_HEADER + "Trying to find solver at default location..." + NEW_LINE);
                checkSolverAtDefaultFolder(solver);
            }
        }

        txt_info.append(NEW_LINE);
    }

    private void checkSolverAtDefaultFolder(JOMSolver solver)
    {
        String message;

        if (isJNAPathSet)
        {
            txt_info.append(MESSAGE_HEADER + "Checking for solver at JNA library path: " + JNAPath + NEW_LINE);
            message = callJOM(solver, JNAPath);

            if (message.isEmpty())
            {
                txt_info.append(MESSAGE_HEADER + "Solver " + solver.name().toUpperCase() + " has been found at directory: " + JNAPath + NEW_LINE);
            } else
            {
                txt_info.append(WARNING_HEADER + "Solver " + solver.name().toUpperCase() + " could not be found at directory: " + JNAPath + NEW_LINE);
            }
        } else
        {
            txt_info.append(WARNING_HEADER + "JNA library path not set. Ignoring..." + NEW_LINE);
        }

        if (isJAVAPathSet)
        {
            txt_info.append(MESSAGE_HEADER + "Checking for solver at JAVA library path: " + JAVAPath + NEW_LINE);

            final List<String> strings = splitPath(JAVAPath);
            if (strings != null)
            {
                for (String separatedPath : strings)
                {
                    message = callJOM(solver, separatedPath);

                    if (message.isEmpty())
                    {
                        txt_info.append(MESSAGE_HEADER + "Solver " + solver.name().toUpperCase() + " has been found at directory: " + separatedPath + NEW_LINE);
                    } else
                    {
                        txt_info.append(WARNING_HEADER + "Solver " + solver.name().toUpperCase() + " could not be found at directory: " + separatedPath + NEW_LINE);
                    }
                }
            } else
            {
                throw new RuntimeException("Internal: ");
            }
        } else
        {
            txt_info.append(WARNING_HEADER + "JAVA library path not set. Ignoring..." + NEW_LINE);
        }

        txt_info.append(MESSAGE_HEADER + "Checking for solver by using system defaults..." + NEW_LINE);

        switch (currentOS)
        {

            case windows:
                message = callJOM(solver, solver.name() + ".dll");
                break;
            case linux:
            case macintosh:
                message = callJOM(solver, "lib" + solver.name());
                break;
            default:
            case unknown:
                return;
        }

        if (message.isEmpty())
        {
            txt_info.append(MESSAGE_HEADER + "Solver " + solver.name().toUpperCase() + " has been found." + NEW_LINE);
        } else
        {
            txt_info.append(WARNING_HEADER + "Solver " + solver.name().toUpperCase() + " could not be found" + NEW_LINE);
        }
    }

    private String callJOM(JOMSolver solver, String path)
    {
        String message;

        switch (solver)
        {
            case glpk:
                message = SolverTester.check_glpk(path);
                break;
            case ipopt:
                message = SolverTester.check_ipopt(path);
                break;
            case cplex:
                message = SolverTester.check_cplex(path);
                break;
            case xpress:
                message = SolverTester.check_xpress(path);
                break;
            default:
                throw new RuntimeException("Unknown solver. Cannot proceed...");
        }

        return message;
    }

    private List<String> splitPath(final String path)
    {
        if (currentOS == OS.linux)
        {
            final String[] ideSplit = path.split("::");

            final List<String> separatedPaths = new ArrayList<>();
            for (String s : ideSplit)
            {
                final String[] aux = s.split(":");

                for (String string : aux)
                {
                    separatedPaths.add(string);
                }
            }

            return separatedPaths;
        }

        return null;
    }

    private static Pair<OS, String> getOS()
    {
        final String osName = System.getProperty("os.name");
        final String osNameLowerCase = osName.toLowerCase();

        if (osNameLowerCase.startsWith("windows"))
        {
            return Pair.of(OS.windows, osName);
        } else if (osNameLowerCase.startsWith("linux"))
        {
            return Pair.of(OS.linux, osName);
        } else if (osNameLowerCase.startsWith("mac"))
        {
            return Pair.of(OS.macintosh, osName);
        } else
        {
            return Pair.of(OS.unknown, "");
        }
    }
}
