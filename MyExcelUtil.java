package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class MyExcelUtil {
	static JFileChooser fileChooser;
	static {
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("������Excel");
		// �Ƿ�����ѡ�����ļ�
		fileChooser.setMultiSelectionEnabled(false);
		// �ӿ����ļ����������б����Ƴ� AcceptAll �ļ�������
		fileChooser.setAcceptAllFileFilterUsed(false);
		// ���Excel������
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Excel���(*.xls)", "xls"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Excel���(*.xlsx)", "xlsx"));
	}

	public static void Export2Excel(JTable table, String sheetName)
			throws IOException {
		int maxCount = 65535;
		String filename = null;
		fileChooser.setDialogTitle("����");
		int returnVal = fileChooser.showSaveDialog(table.getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			filename = fileChooser.getSelectedFile().getPath();
			if (!filename.endsWith(".xls")) {
				filename = filename + ".xls";
			}
		} else {
			return;
		}

		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		WorkbookSettings workbookSettings = new WorkbookSettings();
		workbookSettings.setUseTemporaryFileDuringWrite(true);
		WritableWorkbook wwb = Workbook.createWorkbook(file, workbookSettings);
		int dataCount = table.getRowCount();// table��������
		int sheetCount = dataCount / maxCount;// sheetҳ��
		try {
			for (int sheetNum = 0; sheetNum <= sheetCount; sheetNum++) {
				WritableSheet ws = wwb.createSheet(sheetName + sheetNum,
						sheetNum);
				for (int i = 0; i < table.getColumnCount(); i++) {
					try {
						Label label = new Label(i, 0, table.getColumnName(i));
						ws.addCell(label);
					} catch (RowsExceededException e) {
						e.printStackTrace();
						continue;
					} catch (WriteException e) {
						e.printStackTrace();
						continue;
					}
				}

				int off = sheetNum * maxCount;// sheet��ʼ�ж�Ӧtable����
				int max = Math.min(dataCount, off + maxCount);// ȡС
				int sheetRowNum = 0;// ÿҳsheet�м���
				for (int i = off; i < max; i++) {
					sheetRowNum++;
					for (int j = 0; j < table.getColumnCount(); j++) {
						try {
							if (table.getValueAt(i, j) == null) {
								ws.addCell(new Label(j, sheetRowNum, null));
							} else if (table.getValueAt(i, j).getClass()
									.equals(Integer.class)) {
								ws.addCell(new Number(j, sheetRowNum,
										(Integer) table.getValueAt(i, j)));
							} else if (table.getValueAt(i, j).getClass()
									.equals(Float.class)) {
								ws.addCell(new Number(j, sheetRowNum,
										(Float) table.getValueAt(i, j)));
							} else if (table.getValueAt(i, j).getClass()
									.equals(Long.class)) {
								ws.addCell(new Number(j, sheetRowNum,
										(Long) table.getValueAt(i, j)));
							} else if (table.getValueAt(i, j).getClass()
									.equals(Double.class)) {
								ws.addCell(new Number(j, sheetRowNum,
										(Double) table.getValueAt(i, j)));
							} else if (table.getValueAt(i, j).getClass()
									.equals(BigDecimal.class)) {
								// Ҫ��BigDecimalת����Double����
								ws.addCell(new Number(j, sheetRowNum,
										((BigDecimal) table.getValueAt(i, j))
												.doubleValue()));
							} else if (table.getValueAt(i, j).getClass()
									.equals(String.class)) {
								ws.addCell(new Label(j, sheetRowNum,
										(String) table.getValueAt(i, j)));
							} else if (table.getValueAt(i, j).getClass()
									.equals(Timestamp.class)) {
								ws.addCell(new Label(j, sheetRowNum, String
										.valueOf(table.getValueAt(i, j))
										.substring(0, 19)));
							}
						} catch (RowsExceededException e) {
							e.printStackTrace();
							// continue;
						} catch (WriteException e) {
							e.printStackTrace();
							// continue;
						}
					}
				}
			}
			// ������д���ļ���
			wwb.write();
			// wwb.removeSheet(sheetNum);
			JOptionPane.showMessageDialog(null, "�����ɹ�", "�ɹ�",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e1) {
			throw new IOException("��һ����������ʹ�ô�Excel�ļ��������޷�����");
		} finally {
			// ��wwb�ر�
			if (wwb != null) {
				try {
					wwb.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * ��Excel�е�������
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static List<String[]> Import4Excel() throws IOException {
		String filename = null;
		fileChooser.setDialogTitle("����");
		int returnVal = fileChooser.showOpenDialog(null);// TODO
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			filename = fileChooser.getSelectedFile().getPath();
		} else {
			return null;
		}
		List<String[]> paras = null;
		InputStream stream = null;
		Workbook rwb = null;
		try {
			stream = new FileInputStream(filename);
			// ����һ��workbook���ȡexcel�ļ�
			rwb = Workbook.getWorkbook(stream);
			// �õ���i��������
			Sheet st = rwb.getSheet(0);// ���������ַ�����ȡsheet��,1Ϊ���֣��±꣬��0��ʼ

			// �õ�st������
			int rowNum = st.getRows();
			// �õ�st������
			int colNum = st.getColumns();

			paras = new ArrayList<String[]>();
			// ��ѭ��
			for (int i = 1; i < rowNum; i++) {
				String[] para = new String[colNum];
				// ��ѭ��
				for (int j = 0; j < colNum; j++) {
					// �õ���j�е�i�е�����
					Cell cell = st.getCell(j, i);
					if (cell.getContents().equals("")
							|| cell.getContents() == null) {
						para[j] = null;
					} else {
						para[j] = cell.getContents();
					}
				}
				paras.add(para);
			}
		} catch (FileNotFoundException e) {
			throw new IOException("�ļ�������");
		} catch (BiffException e) {
			throw new IOException("�ļ���ʽ����ȷ");
		} catch (IOException e) {
			throw new IOException("�ļ���ȡʧ��");
		} finally {
			if (rwb != null) {
				rwb.close();
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return paras;
	}
}
