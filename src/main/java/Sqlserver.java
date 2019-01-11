import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.DocxRenderData;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.TextRenderData;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把数据库中的表结构导出word中
 * @author MOSHUNWEI
 * @version 1.0
 */
public class Sqlserver
{
	private static final String baseUrl = "jdbc:sqlserver://10.1.1.14:1433";
	private static final String destination = "/Users/gxkai/Desktop";
	private static final String user = "sa";
	private static final String password = "ks@57735808#301";
    public static void main( String[] args ) throws IOException
    {
    	String sql = "select name from master..sysdatabases where name  NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )";
		ResultSet rs = SQLServerUtils.getResultSet(SQLServerUtils.getConnnection(baseUrl, user, password),sql);
		List<Map<String, String>> list = getDbName(rs);
		for(Map<String, String> str : list){
			SQLServer(str.get("name"));
		}
    }

	private static void SQLServer(String dbName) throws IOException{
    	String url = baseUrl + ";DatabaseName=" + dbName;
		//默认生成的文件名
		String outFile = destination+"/" + dbName + ".docx";

		//查询表的名称以及一些表需要的信息
		String sqlserverSql1 = "SELECT table_name as table_name,table_type as table_type,table_schema as engine FROM INFORMATION_SCHEMA.TABLES";

		String sqlserverSql2 = "SELECT ordinal_position,column_name ,is_nullable, column_default,data_type,character_maximum_length\n" +
				"FROM information_schema.columns where table_name = '";

		ResultSet rs = SQLServerUtils.getResultSet(SQLServerUtils.getConnnection(url, user, password),sqlserverSql1);

		List<Map<String, String>> list = getTableName(rs);
		RowRenderData header = getHeader();
		Map<String,Object> datas = new HashMap<String, Object>();
		datas.put("title", dbName);
		List<Map<String,Object>> tableList = new ArrayList<Map<String,Object>>();
		int i = 0;
		for(Map<String, String> str : list){
			i++;
			System.out.println(str.get("table_name").replaceAll("'",""));
			String sql = sqlserverSql2+str.get("table_name").replaceAll("'","")+"'";
			ResultSet set = SQLServerUtils.getResultSet(SQLServerUtils.getConnnection(url, user, password),sql);
			List<RowRenderData> rowList = getRowRenderData(set);
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("no", ""+i);
//			data.put("table_comment",str.get("table_comment")+"");
			data.put("engine",str.get("engine")+"");
//			data.put("table_collation",str.get("table_collation")+"");
			data.put("table_type",str.get("table_type")+"");
			data.put("name", new TextRenderData(str.get("table_name"), POITLStyle.getHeaderStyle()));
			data.put("table", new MiniTableRenderData(header, rowList));
			tableList.add(data);
		}

		datas.put("tablelist", new DocxRenderData(FileUtils.Base64ToFile(outFile,false), tableList));
		XWPFTemplate template = XWPFTemplate.compile(FileUtils.Base64ToInputStream()).render(datas);

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally {
			try {
				template.write(out);
				out.flush();
				out.close();
				template.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
     * table的表头
     * @return RowRenderData
     */
    private static RowRenderData getHeader(){
    	RowRenderData header = RowRenderData.build(
				new TextRenderData("序号", POITLStyle.getHeaderStyle()),
				new TextRenderData("字段名称", POITLStyle.getHeaderStyle()),
//				new TextRenderData("字段描述", POITLStyle.getHeaderStyle()),
				new TextRenderData("字段类型", POITLStyle.getHeaderStyle()),
				new TextRenderData("长度", POITLStyle.getHeaderStyle()),
				new TextRenderData("允许空", POITLStyle.getHeaderStyle()),
				new TextRenderData("缺省值", POITLStyle.getHeaderStyle()));
		header.setStyle(POITLStyle.getHeaderTableStyle());
		return header;
    }
    
    /**
     * 获取一张表的结构数据
     * @param set
     * @return List<RowRenderData>
     */
    private static List<RowRenderData> getRowRenderData(ResultSet set) {
    	List<RowRenderData> result = new ArrayList<RowRenderData>();
    	
    	try {
    		int i = 0;
			while(set.next()){
				i++;
				RowRenderData row = RowRenderData.build(
						new TextRenderData(set.getString("ordinal_position")+""),
						new TextRenderData(set.getString("column_name")+""),
//						new TextRenderData(set.getString("column_comment")+""),
						new TextRenderData(set.getString("data_type")+""),
						new TextRenderData(set.getString("character_maximum_length")+""),
						new TextRenderData(set.getString("is_nullable")+""),
						new TextRenderData(set.getString("column_default")+"")
						);
				if(i%2==0){
					row.setStyle(POITLStyle.getBodyTableStyle());
					result.add(row);
				}else{
					result.add(row);
				}
			
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		return result;
	}

    /**
     * 获取数据库的所有表名及表的信息
     * @param rs
     * @return list
     */
    private static List<Map<String,String>> getTableName(ResultSet rs){
    	List<Map<String,String>> list = new ArrayList<Map<String,String>>();
    	
    	try {
			while(rs.next()){
				Map<String,String> result = new HashMap<String,String>();
				result.put("table_name", rs.getString("table_name")+"");
				result.put("table_type", rs.getString("table_type")+"");
				result.put("engine", rs.getString("engine")+"");
//				result.put("table_collation", rs.getString("table_collation")+"");
//				result.put("table_comment", rs.getString("table_comment")+"");
//				result.put("create_options", rs.getString("create_options")+"");
				list.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return list;
    }

	private static List<Map<String,String>> getDbName(ResultSet rs){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();

		try {
			while(rs.next()){
				Map<String,String> result = new HashMap<String,String>();
				result.put("name", rs.getString("name")+"");
				list.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

}
