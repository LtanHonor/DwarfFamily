package de.ninjasoft.dwarffamily;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class DwarfListWindow extends JFrame implements MouseListener {

    /**
     * 
     */
    private static final long serialVersionUID = -1854336400936388884L;

    private ArrayList<Dwarf> dwarfList;

    AbstractListModel<String> dataModel = new AbstractListModel<String>() {
            private static final long serialVersionUID = 7104965065235986310L;
            public int getSize() { return dwarfList.size(); }
            public String getElementAt(int index) { return dwarfList.get(index).getCasedName(); }
     };

    private JList<String> dataList = new JList<String>(dataModel);
    private JTextPane textArea = new JTextPane();

    private JScrollPane listScrollPane = new JScrollPane(dataList);
    private JScrollPane textAreaScrollPane = new JScrollPane(textArea);

    private StyledDocument doc;
    private Style stylePlain, styleBold;

    public DwarfListWindow(ArrayList<Dwarf> dwarves) throws HeadlessException {
            dwarfList = dwarves;

            dataList.addMouseListener(this);

            textArea.setText("Select a dwarf on the list.");
            textArea.setEditable(false);
            textArea.addMouseListener(this);

            doc = textArea.getStyledDocument();
            stylePlain = textArea.addStyle("regular", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
            styleBold = textArea.addStyle("bold", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
            StyleConstants.setBold(styleBold, true);

            JPanel topArea = new JPanel();
            topArea.setLayout(new BoxLayout(topArea, BoxLayout.X_AXIS));

            JPanel bottomArea = new JPanel();
            bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.X_AXIS));

            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            getContentPane().setMinimumSize(new Dimension(800, 600));

            topArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            bottomArea.add(listScrollPane);
            bottomArea.add(textAreaScrollPane);

            getContentPane().add(topArea);
            getContentPane().add(bottomArea);
            pack();
            
            this.setMinimumSize(new Dimension(800, 600));
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if(e.getSource() == dataList)
        {
            try {
                int index = dataList.locationToIndex(e.getPoint());
                Dwarf dwarf = dwarfList.get(index);
                showDwarfData(dwarf);
            } catch (BadLocationException ex) {
                Logger.getLogger(DwarfListWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(e.getSource() == textArea)
        {
            Element ele = doc.getCharacterElement(textArea.viewToModel(e.getPoint()));
            AttributeSet as = ele.getAttributes();
            ChildrenLinkListener fla = (ChildrenLinkListener)as.getAttribute("linkact");
            if(fla != null)
            {
                fla.execute();
            }
        }
    }

    public void showDwarfData(Dwarf dwarf) throws BadLocationException
    {
        doc.remove(0, doc.getLength());

        doc.insertString(doc.getLength(), "ID: "    + 	dwarf.getId() + "\n", stylePlain);
        doc.insertString(doc.getLength(), "Name: ", stylePlain);
        doc.insertString(doc.getLength(), dwarf.getCasedName() + "\n", styleBold);
        doc.insertString(doc.getLength(), "Gender: "+ 	dwarf.getCasedGender() + "\n", stylePlain);

        doc.insertString(doc.getLength(), "Mother: ", stylePlain);
        if(dwarf.getMother() != null)
        {
            Style regularBlue = textArea.addStyle("regularBlue", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
            StyleConstants.setForeground(regularBlue, Color.BLUE);
            StyleConstants.setUnderline(regularBlue, true);
            regularBlue.addAttribute("linkact", new ChildrenLinkListener(dwarf.getMother().getName(), this));
            
            doc.insertString(doc.getLength(), dwarf.getMother().getCasedName() + "\n", regularBlue);
        }
        else doc.insertString(doc.getLength(), "unknown\n", stylePlain);

        doc.insertString(doc.getLength(), "Father: ", stylePlain);
        if(dwarf.getFather() != null)
        {
            Style regularBlue = textArea.addStyle("regularBlue", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
            StyleConstants.setForeground(regularBlue, Color.BLUE);
            StyleConstants.setUnderline(regularBlue, true);
            regularBlue.addAttribute("linkact", new ChildrenLinkListener(dwarf.getFather().getName(), this));
            
            doc.insertString(doc.getLength(), dwarf.getFather().getCasedName() + "\n", regularBlue);
        }
        else doc.insertString(doc.getLength(), "unknown\n", stylePlain);

        doc.insertString(doc.getLength(), "Birth: " + dwarf.getBirthday() + " " + dwarf.getBirthyear() + "\n", stylePlain);
        doc.insertString(doc.getLength(), "Death: " + dwarf.getBirthday() + " " + dwarf.getDeathyear() + "\n", stylePlain);

        if(dwarf.getChildren() != null && dwarf.getChildren().size() > 0)
        {
            doc.insertString(doc.getLength(), "Children: \n", stylePlain);
            for(int i=0; i<dwarf.getChildren().size(); i++)
            {
                Style regularBlue = textArea.addStyle("regularBlue", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
                StyleConstants.setForeground(regularBlue, Color.BLUE);
                StyleConstants.setUnderline(regularBlue, true);
                regularBlue.addAttribute("linkact", new ChildrenLinkListener(dwarf.getChildren().get(i).getName(), this));
            
                doc.insertString(doc.getLength(), "\t", stylePlain);
                doc.insertString(doc.getLength(), dwarf.getChildren().get(i).getCasedName() + "\n", regularBlue);
            }
        }
        else
        {
            doc.insertString(doc.getLength(), "No offspring.\n", stylePlain);
        }
    }

    public void selectDwarfWithName(String name)
    {
        name = name.toLowerCase();
        for(int i=0; i<dwarfList.size(); i++)
        {
            if(dwarfList.get(i).getName().equals(name))
            {
                try {
                    dataList.setSelectedIndex(i);
                    showDwarfData(dwarfList.get(i));
                    return;
                } catch (BadLocationException ex) {
                    Logger.getLogger(DwarfListWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}

class ChildrenLinkListener extends AbstractAction
{
    private final String textLink;
    private final DwarfListWindow window;

    ChildrenLinkListener(String textLink, DwarfListWindow win)
    {
        this.textLink = textLink;
        this.window = win;
    }

    protected void execute()
    {
        window.selectDwarfWithName(textLink);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        execute();
    }
}