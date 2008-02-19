package jednostki;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.I3B1S0.path.common.DirectionXY;
import com.I3B1S0.path.pathfinder.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import sterowanie.Zdarzenie;
import model.teren.Kwadrat;
import model.teren.Teren_ZL;


public class JednostkaWs
{
    private static final String LOG_CONFIG_FILENAME = "log4j.properties";
    static private Logger logger = null;
    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/SCENA";
    private static String username = "root";
    private static String password = "123";
    private static java.sql.Connection con = null;
    private static String polozenieBeginning;
    private static String polozenie1;
    private static String polozenie2;
    private static String polozenie3;
    private static String polozenie4;
    private static int polBX, polBY, pol1X, pol1Y, pol2X, pol2Y, pol3X, pol3Y, pol4X, pol4Y;
    private static boolean czyKwadrat;
    
 
  public JednostkaWs()
  {
    if (logger == null)
    {
      PropertyConfigurator.configureAndWatch(LOG_CONFIG_FILENAME);
      logger = Logger.getLogger(this.getClass());
      logger.info("jednostka started");
    } 
  }
 
 public Zdarzenie[] init(Zdarzenie z)
 {
	 
   try{
       Class.forName(driver).newInstance();
      
       con = DriverManager.getConnection(url, username, password);
       
       Statement s = con.createStatement();
       
       //sprawdzamy, czy w bazie sa dwie pomocnicze tabele. Jesli nie, to je tworzymy
       //pierwsza z tabel - informacje o rozpoznanych jednostkach
       s.executeUpdate("Create table if not exists sc_rozp_jednostki( " +
    		   "id_jednostki varchar(10) not null, " +
    		   "strona_konf varchar(20) not null, " +
    		   "PRIMARY KEY(id_jednostki)) "); 
      
       //druga tabela - nasza aktualna pozycja (tak na wszelki wypadek)
       s.executeUpdate("Create table if not exists sc_nasza_poprz_poz( " +
    		   "id_jednostki varchar(10) not null, " +
    		   "strona_konf varchar(20) not null, " +
    		   "pozX_jedn int(10) not null, " +
    		   "pozY_jedn int(10) not null, " +
	   		   "PRIMARY KEY(id_jednostki)) ");
       
       ResultSet rs = s.executeQuery("select * from SC_SCENARIUSZE S, SC_JEDNOSTKI J " +
               "where S.NUMER = '" + z.getIdScenariusza()  + "'  " +
               "and S.ID = J.ID_SCENA " +
               "and J.STRONA = '" + z.getIdStronyKonfliktu()  + "'  " +
               "and J.ID = '" + z.getIdJednostki()  + "'" );
            
       if (rs.next()) {
    	   polozenieBeginning = rs.getString("POLOZENIE_XY");
       }

       rs = s.executeQuery("select * from sc_z_rozpoznanie r, sc_zadania z" +
               " where z.id_jednostki = '" + z.getIdJednostki()  + "'  " +
               " and z.id = r.id_z ");
          
       if (rs.next()) {
         polozenie1 = rs.getString("OO_PUNKT1");
         polozenie2 = rs.getString("OO_PUNKT2");
         polozenie3 = rs.getString("OO_PUNKT3");
         polozenie4 = rs.getString("OO_PUNKT4");
       }
       
       //przejscie na wspolrzedne xy z poloznie1
       float geoPol1X = Float.parseFloat(polozenie1.substring(8, 10)) +
       Float.parseFloat(polozenie1.substring(10, 12)) / 60.0f+
       Float.parseFloat(polozenie1.substring(12, 14)) / 3600.0f;
            
       float geoPol1Y = Float.parseFloat(polozenie1.substring(0, 2)) +
       Float.parseFloat(polozenie1.substring(2, 4)) / 60.0f+
       Float.parseFloat(polozenie1.substring(4, 6)) / 3600.0f;
       // przejscie na wspolrzedne xy z poloznie1
       float geoPol2X = Float.parseFloat(polozenie2.substring(8, 10)) +
       Float.parseFloat(polozenie2.substring(10, 12)) / 60.0f+
       Float.parseFloat(polozenie2.substring(12, 14)) / 3600.0f;
        
       float geoPol2Y = Float.parseFloat(polozenie2.substring(0, 2)) +
       Float.parseFloat(polozenie2.substring(2, 4)) / 60.0f+
       Float.parseFloat(polozenie2.substring(4, 6)) / 3600.0f;
       //przejscie na wspolrzedne xy z poloznie1
       float geoPol3X = Float.parseFloat(polozenie3.substring(8, 10)) +
       Float.parseFloat(polozenie3.substring(10, 12)) / 60.0f+
       Float.parseFloat(polozenie3.substring(12, 14)) / 3600.0f;
        
       float geoPol3Y = Float.parseFloat(polozenie3.substring(0, 2)) +
       Float.parseFloat(polozenie3.substring(2, 4)) / 60.0f+
       Float.parseFloat(polozenie3.substring(4, 6)) / 3600.0f;
       //przejscie na wspolrzedne xy z poloznie1
       float geoPol4X = Float.parseFloat(polozenie4.substring(8, 10)) +
       Float.parseFloat(polozenie4.substring(10, 12)) / 60.0f+
       Float.parseFloat(polozenie4.substring(12, 14)) / 3600.0f;
        
       float geoPol4Y = Float.parseFloat(polozenie4.substring(0, 2)) +
       Float.parseFloat(polozenie4.substring(2, 4)) / 60.0f+
       Float.parseFloat(polozenie4.substring(4, 6)) / 3600.0f;
       
       float geoY = Float.parseFloat(polozenieBeginning.substring(0, 2)) +
       Float.parseFloat(polozenieBeginning.substring(2, 4)) / 60.0f+
       Float.parseFloat(polozenieBeginning.substring(4, 6)) / 3600.0f;
       
       float geoX = Float.parseFloat(polozenieBeginning.substring(8, 10)) +
       Float.parseFloat(polozenieBeginning.substring(10, 12)) / 60.0f+
       Float.parseFloat(polozenieBeginning.substring(12, 14)) / 3600.0f;
       
       polBX = Teren_ZL.getIdKwX(geoX);
       polBY = Teren_ZL.getIdKwY(geoY);
       
       pol1X = Teren_ZL.getIdKwX(geoPol1X);
       pol1Y = Teren_ZL.getIdKwY(geoPol1Y);
       pol2X = Teren_ZL.getIdKwX(geoPol2X);
       pol2Y = Teren_ZL.getIdKwY(geoPol2Y);
       pol3X = Teren_ZL.getIdKwX(geoPol3X);
       pol3Y = Teren_ZL.getIdKwY(geoPol3Y);
       pol4X = Teren_ZL.getIdKwX(geoPol4X);
       pol4Y = Teren_ZL.getIdKwY(geoPol4Y);
       
       System.out.println("z " + polBX + " " + polBY + " " + pol4X + " " + pol4Y);
       List<DirectionXY> path = null;
       try{
    	   path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol1X, pol1Y);
       }
       catch(Exception ex)
       {
    	   ex.printStackTrace();
       }
       int[] pathSize = new int[4];
       if(path == null)
          pathSize[0] = 0;
       else
           pathSize[0] = path.size();
               
       path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol2X, pol2Y);
       if(path == null)
            pathSize[1] = 0;
       else
           pathSize[1] = path.size();
       
       path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol3X, pol3Y);
       if(path == null)
           pathSize[2] = 0;
       else
            pathSize[2] = path.size();
           
       path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol4X, pol4Y);
       if(path == null)
            pathSize[3] = 0;
       else
            pathSize[3] = path.size();
       
       int min = 0;
       for (int i=0; i< 3; i++ ){
           if(pathSize[i+1] < pathSize[min])
             min = i+1;
       }
       
       if(pathSize[min] == 0)
            czyKwadrat = true;
       else
            czyKwadrat = false;
       
       //nowe zdarzenie
       Zdarzenie event = new Zdarzenie();
       event.setTypZdarzenia(0);
       event.setIdJednostki(z.getIdJednostki());
	   event.setIdStronyKonfliktu(z.getIdStronyKonfliktu());
	   
	   //polozenie startowe jednostki w tym przypadku to ustawienie poczatkowe jednostki
	   event.setPolozenieStartoweJednostkiX(polBX);
	   event.setPolozenieStartoweJednostkiY(polBY);
	   DirectionXY dirXY = null;
	   
       if(czyKwadrat)
       {
    	   //sprawdzamy, wspolrzedna y ktorego z punktow jest taka sama jak wspolrzedna y
    	   //punktu w ktorym stoimy. Jak sie tego dowiemy, to idziemy w tamtym kierunku
    	   if(polBY == pol1Y && min != 0)
    		   dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol1X, pol1Y);

    	   if(polBY == pol2Y && min != 1)
    		   dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol2X, pol2Y);
    	   
    	   if(polBY == pol3Y && min != 2)
    		   dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol3X, pol3Y);
    	   
    	   if(polBY == pol4Y && min != 3)
    	       dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol4X, pol4Y);
    	   
       }
       else
       {
    	   //sprawdzamy do ktorego punktu mamy najblizej i wykonujemy krok w tamtym kierunku
    	   switch(min)
    	   {
    	       case 0:
    	    	   dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol1X, pol1Y);
    	    	   break;
    	       case 1:
    	    	   dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol2X, pol2Y);
    	    	   break;
    	       case 2:
    	    	   dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol3X, pol3Y);
    	    	   break;
    	       case 3:
        	       dirXY = PathFinder.getInstance().returnNextStep(polBX, polBY, pol4X, pol4Y);
        	       break;
        	   default:
        		   break;
    	   }
       }
       
       event.setPunktDocelowyPrzemieszczeniaX(dirXY.getX());
	   event.setPunktDocelowyPrzemieszczeniaY(dirXY.getY());
	   
	   //zapisujemy pozycje do ktorej sie udajemy jako aktualna nasza pozycje
	   s.executeUpdate("insert into sc_nasza_poprz_poz values(" +
			   "'" + z.getIdJednostki() + "', '" + z.getIdStronyKonfliktu() + "', " +
			   polBX + ", " + polBY + ")");
	   
       con.close();
       
       return new Zdarzenie[] {event};
   }
   catch(Exception e)
   {
     System.out.println(e.getMessage());
   }
   
   //jesli wszystko bedzie ok, to ten return nigdy sie nie wykona
   return null;
 }
 
  public Zdarzenie[] krok(Zdarzenie z)
  {
    switch(z.getTypZdarzenia())
    {
      case 1://PRZEMIESZCZENIE_STOP
        try{
          Class.forName(driver).newInstance();
          con = DriverManager.getConnection(url, username, password);
          Statement s = con.createStatement();
          s.executeUpdate("insert into sc_nasza_poprz_poz set " +
                  "id_jednostki = '"+z.getIdJednostki()+"', " +
                  "strona_konf = '"+z.getIdStronyKonfliktu()+"', " +
                  "pozX_jedn = '"+z.getPolozenieStartoweJednostkiX()+"', " +
                  "pozY_jedn = '"+z.getPolozenieStartoweJednostkiY()+"' ON DUPLICATE KEY UPDATE " +
                  "pozX_jedn = '"+z.getPolozenieStartoweJednostkiX()+"', " +
                  "pozY_jedn = '"+z.getPolozenieStartoweJednostkiY()+"'" );
          
                 
          con.close();
          return null;
        }
        catch(Exception e)
        {
              
        }
        break;
      case 2://ROZPOZNANIE
        try{
          Class.forName(driver).newInstance();
          con = DriverManager.getConnection(url, username, password);
          Statement s = con.createStatement();
          //dodajemy do bazy id rozpoznanej jednostki i jej id_strony
          s.executeUpdate("insert into sc_rozp_jednostki set " +
                          "id_jednostki = '"+ z.getIdJednostkiRozpoznanej()+"',"  +
                          "strona_konf = '"+ z.getIdStronyKonfliktuJednostkiRozpoznanej()+ "'");
          con.close();
          return null;
        }
        catch(Exception e)
        {
                 
        }
        break; 
             
      case 3://JEDNOSTKA_WIDOCZNA
        try{
          int prevX;
          int prevY;
          Class.forName(driver).newInstance();
          con = DriverManager.getConnection(url, username, password);
          Statement s = con.createStatement();
            
          ResultSet rs = s.executeQuery("select * from sc_nasza_poprz_poz S " +
                                        "where S.id_jednostki = '" + z.getIdJednostki()  + "'  " +
                                        "and S.strona_konf = '" + z.getIdStronyKonfliktu()  + "' " );
                 
          if (rs.next()) {
            prevX = rs.getInt("pozX_jedn");
            prevY = rs.getInt("pozY_jedn");
            z.setPunktDocelowyPrzemieszczeniaX(prevX);
            z.setPunktDocelowyPrzemieszczeniaY(prevY);
            z.setTypZdarzenia(0);
            con.close();
            Zdarzenie[] zd = new Zdarzenie[1];
            zd[0] = z;
            return zd;
          }
          return null;
        }
        catch(Exception e)
        {
                 
        }
        break; 
      default:
        
        return null;
    }       
    return null;
  }
}


//http://localhost:8080/wsJednostka/services/JednostkaWs
