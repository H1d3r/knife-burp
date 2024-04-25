package config;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import burp.BurpExtender;

import static config.ConfigTableModel.titles;


public class ConfigTable extends JTable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;


    public ConfigTable(ConfigTableModel ConfigTableModel) {
        super(ConfigTableModel);
        this.setColumnModel(columnModel);
        this.setFillsViewportHeight(true);//在table的空白区域显示右键菜单
        //https://stackoverflow.com/questions/8903040/right-click-mouselistener-on-whole-jtable-component
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setBorder(new LineBorder(new Color(0, 0, 0)));

        addClickSort();
        registerListeners();
        //switchEnable();//no need
        //table.setupTypeColumn()//can't set here, only can after table data loaded.
        //tableHeaderLengthInit();//can't set here, only can after table data loaded.
    }

    @Override
    public void changeSelection(int row, int col, boolean toggle, boolean extend) {
        super.changeSelection(row, col, toggle, extend);
    }

    public int[] getSelectedModelRows() {
        int[] rows = getSelectedRows();

        for (int i = 0; i < rows.length; i++) {
            rows[i] = convertRowIndexToModel(rows[i]);//转换为Model的索引，否则排序后索引不对应〿
        }
        Arrays.sort(rows);//升序
        return rows;
    }

    private void addClickSort() {
        TableRowSorter<ConfigTableModel> sorter = new TableRowSorter<>((ConfigTableModel) this.getModel());
        ConfigTable.this.setRowSorter(sorter);
    }

    /**
     * 需要在数据加载后，即setModel后才有效果!
     */
    public void tableHeaderLengthInit() {
        Font f = this.getFont();
        FontMetrics fm = this.getFontMetrics(f);
        int width = fm.stringWidth("A");//一个字符的宽度
        for (int index = 0; index < this.getColumnCount(); index++) {
            TableColumn column = this.getColumnModel().getColumn(index);

            if (column.getIdentifier().equals("#")) {
                column.setMaxWidth(width * "100".length());
            }

            if (column.getIdentifier().equals("Enable")) {
                column.setMaxWidth(width * "Enable++".length());
                //需要预留排序时箭头符合的位置，2个字符宽度
            }

            if (column.getIdentifier().equals("Type")) {
                column.setPreferredWidth(width * ConfigEntry.Action_If_Base_URL_Matches_Append_To_header_value.length());
            }
        }
        //this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//配合横向滚动条
    }


    private void registerListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ConfigTable target = (ConfigTable) e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    if (titles[column].equals("Enable")) {
                        boolean value = (boolean) getValueAt(row, column);
                        setValueAt(!value, row, column);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //no need
            }
        });
    }


    public void setupTypeColumn() {
        //call this function must after table data loaded !!!!
        JComboBox<String> comboBox = new JComboBox<>();

        String[] items = new ConfigEntry().listAllConfigType();
        for (String item : items) {
            comboBox.addItem(item);
        }
        TableColumnModel model = this.getColumnModel();

        int col = Arrays.asList(titles).indexOf("Type");
        DefaultCellEditor editor = new DefaultCellEditor(comboBox);
        editor.setClickCountToStart(2);
        model.getColumn(col).setCellEditor(editor);
    }
}
