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
public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	
    	
    	//使用jar的命令，参数有4个，如果没有4个直接退出，没得商量
    	if(args.length<4){
    		System.exit(0);
    	}
    	
    	Map<String, String> map = Check(args);
    	
    	if(map.get("-t")==null||map.get("-t").equals("mysql")){
    		MySQL(map);    		
    	}else if(map.get("-t").equals("oracle")){
    		Oracle(map);
    	}else{
    		System.exit(0);
    	}
    }
    
    private static void Oracle(Map<String,String> map) throws IOException{
    	//默认生成的文件名
    	String outFile = map.get("-d")+"/数据库表结构(ORACLE).docx";
    	
    	//查询表的名称以及一些表需要的信息
    	String oracleSql1 = "select ut.table_name as table_name,ut.tablespace_name as engine,ut.buffer_pool as table_collation, uc.table_type as table_type,uc.comments as table_comment,ut.last_analyzed as create_options from user_tables ut,user_tab_comments uc where ut.table_name=uc.table_name";
    	
    	String oracleSql2 = "select rownum as ordinal_position,c.nullable as is_nullable,c.data_default as column_default,c.data_type as data_type,c.data_length as character_maximum_length,t.column_name as column_name,t.comments as column_comment from user_col_comments t,user_tab_columns c where c.column_name=t.column_name and c.table_name=t.table_name and t.table_name='";
    	
		ResultSet rs = OracleUtils.getResultSet(OracleUtils.getConnnection(map.get("-u"), map.get("-p")),oracleSql1);
		
		List<Map<String, String>> list = getTableName(rs);
		RowRenderData header = getHeader();
		Map<String,Object> datas = new HashMap<String, Object>();
		datas.put("title", "数据结构表(ORACLE)");
		List<Map<String,Object>> tableList = new ArrayList<Map<String,Object>>();
		int i = 0;
		for(Map<String, String> str : list){
			i++;
			String sql = oracleSql2+str.get("table_name")+"'";
			ResultSet set = OracleUtils.getResultSet(OracleUtils.getConnnection(map.get("-u"), map.get("-p")),sql);
			List<RowRenderData> rowList = getRowRenderData(set);
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("no", ""+i);
			data.put("table_comment",str.get("table_comment")+"");
			data.put("engine",str.get("engine")+"");
			data.put("table_collation",str.get("table_collation")+"");
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
    
    private static void MySQL(Map<String,String> map) throws IOException{
    	//默认生成的文件名
    	String outFile = map.get("-d")+"/数据库表结构(MySQL1).docx";
    	
    	//查询表的名称以及一些表需要的信息
    	String mysqlSql1 = "SELECT table_name, table_type , ENGINE,table_collation,table_comment, create_options FROM information_schema.TABLES WHERE table_schema='"+map.get("-n")+"'";
    	
    	//查询表的结构信息
    	String mysqlSql2 = "SELECT ordinal_position,column_name,column_type, column_key, extra ,is_nullable, column_default, column_comment,data_type,character_maximum_length "
    			+ "FROM information_schema.columns WHERE table_schema='"+map.get("-n")+"' and table_name='";
		
    	
		ResultSet rs = SqlUtils.getResultSet(SqlUtils.getConnnection(map.get("-u"), map.get("-p")),mysqlSql1);
		
		List<Map<String, String>> list = getTableName(rs);
		RowRenderData header = getHeader();
		Map<String,Object> datas = new HashMap<String, Object>();
		datas.put("title", "数据结构表(MySQL)");
		List<Map<String,Object>> tableList = new ArrayList<Map<String,Object>>();
		int i = 0;
		for(Map<String, String> str : list){
			i++;
			String sql = mysqlSql2+str.get("table_name")+"'";
			ResultSet set = SqlUtils.getResultSet(SqlUtils.getConnnection(map.get("-u"), map.get("-p")),sql);
			List<RowRenderData> rowList = getRowRenderData(set);
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("no", ""+i);
			data.put("table_comment",str.get("table_comment")+"");
			data.put("engine",str.get("engine")+"");
			data.put("table_collation",str.get("table_collation")+"");
			data.put("table_type",str.get("table_type")+"");
			data.put("name", new TextRenderData(str.get("table_name"), POITLStyle.getHeaderStyle()));
			data.put("table", new MiniTableRenderData(header, rowList));
			tableList.add(data);
		}

		datas.put("tablelist", new DocxRenderData(FileUtils.Base64ToFile(outFile,true), tableList));
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
     * 检查缺少的参数
     * @param args
     * @return
     */
    private static Map<String, String> Check(String[] args) {
    	Map<String, String> map = new HashMap<String, String>();
    	for(String str: args){
    		String[] split = str.split("=");
    		map.put(split[0], split[1]);
    	}
    	
    	if(!map.containsKey("-n")){
    		System.exit(0);
    	}
    	if(!map.containsKey("-u")){
    		System.exit(0);
    	}
    	if(!map.containsKey("-p")){
    		System.exit(0);
    	}
    	if(!map.containsKey("-d")){
    		System.exit(0);
    	}
    	
    	return map;
	}

	/**
     * table的表头
     * @return RowRenderData
     */
    private static RowRenderData getHeader(){
    	RowRenderData header = RowRenderData.build(
				new TextRenderData("序号", POITLStyle.getHeaderStyle()),
				new TextRenderData("字段名称", POITLStyle.getHeaderStyle()),
				new TextRenderData("字段描述", POITLStyle.getHeaderStyle()),
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
						new TextRenderData(set.getString("column_comment")+""),
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
				result.put("table_collation", rs.getString("table_collation")+"");
				result.put("table_comment", rs.getString("table_comment")+"");
				result.put("create_options", rs.getString("create_options")+"");
				list.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return list;
    }

}
