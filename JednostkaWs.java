package jednostki;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.I3B1S0.path.common.DirectionXY;
import com.I3B1S0.path.pathfinder.*;
import com.I3B1S0.model.droga.*;

//import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import sterowanie.Zdarzenie;


public class JednostkaWs
{
    private static final String LOG_CONFIG_FILENAME = "log4j.properties";
    static private Logger logger = null;
    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/SCENA";
    private static String username = "root";
    private static String password = "";
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
    	         "punkt_docelX int(10) not null, " +
    	         "punkt_docelY int(10) not null, " +
    	         "PRIMARY KEY(id_jednostki)) ");
       
       //trzecia tabela - punkty do zbadania, punkty wzdluz ktorych chodzimy i numer kroku
       s.executeUpdate("Create table if not exists sc_obszar_do_rozp( " +
    	         "id_jednostki varchar(10) not null, " +
    	         "strona_konf varchar(20) not null, " +
    	         "LDX int(10) not null, " +
    	         "LDY int(10) not null, " +
    	         "PDX int(10) not null, " +
    	         "PDY int(10) not null, " +
    	         "LGX int(10) not null, " +
    	         "LGY int(10) not null, " +
    	         "PGX int(10) not null, " +
    	         "PGY int(10) not null, " +
    	         "LRX int(10) not null, " +
    	         "LRY int(10) not null, " +
    	         "PRX int(10) not null, " +
    	         "PRY int(10) not null, " +
    	         "numer_kroku int(10) not null, " +
    	         "numer_kroku_max int(10) not null, " +
    	         "kierunek int(1) not null, " +
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
       
       
       List<DirectionXY> path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol1X, pol1Y);
       
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
	   
	   
	   int endPointX = 0;
	   int endPointY = 0;
	    
	   if(czyKwadrat)
	   {
	        //sprawdzamy, wspolrzedna y ktorego z punktow jest taka sama jak wspolrzedna y
	        //punktu w ktorym stoimy. Jak sie tego dowiemy, to idziemy w tamtym kierunku
	        if(polBY == pol1Y && min != 0)
	        {
	         path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol1X, pol1Y);
	         endPointX = pol1X;
	         endPointY = pol1Y;
	        }

	        if(polBY == pol2Y && min != 1)
	        {
	         path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol2X, pol2Y);
	         endPointX = pol2X;
	         endPointY = pol2Y;
	        }
	        
	        if(polBY == pol3Y && min != 2)
	        {
	         path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol3X, pol3Y);
	         endPointX = pol3X;
	         endPointY = pol3Y;
	        }
	        
	        if(polBY == pol4Y && min != 3)
	        {
	         path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol4X, pol4Y);
	         endPointX = pol4X;
	         endPointY = pol4Y;
	        }
	        
	       }
	    else
	    {
	        //sprawdzamy do ktorego punktu mamy najblizej i wykonujemy krok w tamtym kierunku
	        switch(min)
	        {
	            case 0:
	             path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol1X, pol1Y);
	             endPointX = pol1X;
	             endPointY = pol1Y;
	             break;
	            case 1:
	             path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol2X, pol2Y);
	             endPointX = pol2X;
	             endPointY = pol2Y;
	             break;
	            case 2:
	             path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol3X, pol3Y);
	             endPointX = pol3X;
	             endPointY = pol3Y;
	             break;
	            case 3:
	             path = PathFinder.getInstance().returnWholePath(polBX, polBY, pol4X, pol4Y);
	             endPointX = pol4X;
	             endPointY = pol4Y;
	                break;
	            default:
	             break;
	        }
	    }
	       
        event.setPunktDocelowyPrzemieszczeniaX(path.get(1).getX());
	    event.setPunktDocelowyPrzemieszczeniaY(path.get(1).getY());
	    
	  //sprawdzimy, ktory z punktow jest rownolegly do punktu najblizszego od nas
	    //dodatkowo sprawdzamy rozlozenie punktow obszaru do rozpoznania
	    int[] X = {pol1X, pol2X, pol3X, pol4X};
	    int[] Y = {pol1Y, pol2Y, pol3Y, pol4Y};
	    
	    int minX = porownanie(X, false);
	    int minY = porownanie(Y, false);
	    int maxX = porownanie(X, true);
	    int maxY = porownanie(Y, true);
	    int[] kwadrat = new int[8];
	    
	    if(pol1X == minX && pol1Y == minY)
	    {
	     kwadrat[0] = pol1X;
	     kwadrat[1] = pol1Y;
	    }
	    if(pol1X == minX && pol1Y == maxY)
	    {
	     kwadrat[4] = pol1X;
	     kwadrat[5] = pol1Y;
	    }
	    if(pol1X == maxX && pol1Y == minY)
	    {
	     kwadrat[2] = pol1X;
	     kwadrat[3] = pol1Y;
	    }
	    if(pol1X == maxX && pol1Y == maxY)
	    {
	     kwadrat[6] = pol1X;
	     kwadrat[7] = pol1Y;
	    }
	    if(pol2X == minX && pol2Y == minY)
	    {
	     kwadrat[0] = pol2X;
	     kwadrat[1] = pol2Y;
	    }
	    if(pol2X == minX && pol2Y == maxY)
	    {
	     kwadrat[4] = pol2X;
	     kwadrat[5] = pol2Y;
	    }
	    if(pol2X == maxX && pol2Y == minY)
	    {
	     kwadrat[2] = pol2X;
	     kwadrat[3] = pol2Y;
	    }
	    if(pol2X == maxX && pol2Y == maxY)
	    {
	     kwadrat[6] = pol2X;
	     kwadrat[7] = pol2Y;
	    }
	    if(pol3X == minX && pol3Y == minY)
	    {
	     kwadrat[0] = pol3X;
	     kwadrat[1] = pol3Y;
	    }
	    if(pol3X == minX && pol3Y == maxY)
	    {
	     kwadrat[4] = pol3X;
	     kwadrat[5] = pol3Y;
	    }
	    if(pol3X == maxX && pol3Y == minY)
	    {
	     kwadrat[2] = pol3X;
	     kwadrat[3] = pol3Y;
	    }
	    if(pol3X == maxX && pol3Y == maxY)
	    {
	     kwadrat[6] = pol3X;
	     kwadrat[7] = pol3Y;
	    }
	    if(pol4X == minX && pol4Y == minY)
	    {
	     kwadrat[0] = pol4X;
	     kwadrat[1] = pol4Y;
	    }
	    if(pol4X == minX && pol4Y == maxY)
	    {
	     kwadrat[4] = pol4X;
	     kwadrat[5] = pol4Y;
	    }
	    if(pol4X == maxX && pol4Y == minY)
	    {
	     kwadrat[2] = pol4X;
	     kwadrat[3] = pol4Y;
	    }
	    if(pol4X == maxX && pol4Y == maxY)
	    {
	     kwadrat[6] = pol4X;
	     kwadrat[7] = pol4Y;
	    }
	    
	       //sprawdzamy punkty rownolegle
	    int lewyX = 0;
	    int lewyY = 0;
	    int prawyX = 0;
	    int prawyY = 0;
	    switch(min)
	    {
	        case 0:
	         if(pol1Y == pol2Y)
	         {
	          if(pol1X > pol2X)
	          {
	           lewyX = pol2X;
	           lewyY = pol2Y;
	           prawyX = pol1X;
	           prawyY = pol1Y;
	          }
	          else
	          {
	           lewyX = pol1X;
	           lewyY = pol1Y;
	           prawyX = pol2X;
	           prawyY = pol2Y;
	          }
	         }
	         if(pol1Y == pol3Y)
	         {
	          if(pol1X > pol3X)
	          {
	           lewyX = pol3X;
	           lewyY = pol3Y;
	           prawyX = pol1X;
	           prawyY = pol1Y;
	          }
	          else
	          {
	           lewyX = pol1X;
	           lewyY = pol1Y;
	           prawyX = pol3X;
	           prawyY = pol3Y;
	          }
	         }
	         if(pol1Y == pol4Y)
	         {
	          if(pol1X > pol4X)
	          {
	           lewyX = pol4X;
	           lewyY = pol4Y;
	           prawyX = pol1X;
	           prawyY = pol1Y;
	          }
	          else
	          {
	           lewyX = pol1X;
	           lewyY = pol1Y;
	           prawyX = pol4X;
	           prawyY = pol4Y;
	          }
	         }
	         break;
	        case 1:
	         if(pol2Y == pol1Y)
	         {
	          if(pol2X > pol1X)
	          {
	           lewyX = pol1X;
	           lewyY = pol1Y;
	           prawyX = pol2X;
	           prawyY = pol2Y;
	          }
	          else
	          {
	           lewyX = pol2X;
	           lewyY = pol2Y;
	           prawyX = pol1X;
	           prawyY = pol1Y;
	          }
	         }
	         if(pol2Y == pol3Y)
	         {
	          if(pol2X > pol3X)
	          {
	           lewyX = pol3X;
	           lewyY = pol3Y;
	           prawyX = pol2X;
	           prawyY = pol2Y;
	          }
	          else
	          {
	           lewyX = pol2X;
	           lewyY = pol2Y;
	           prawyX = pol3X;
	           prawyY = pol3Y;
	          }
	         }
	         if(pol2Y == pol4Y)
	         {
	          if(pol2X > pol4X)
	          {
	           lewyX = pol4X;
	           lewyY = pol4Y;
	           prawyX = pol2X;
	           prawyY = pol2Y;
	          }
	          else
	          {
	           lewyX = pol2X;
	           lewyY = pol2Y;
	           prawyX = pol4X;
	           prawyY = pol4Y;
	          }
	         }
	         break;
	        case 2:
	         if(pol3Y == pol1Y)
	         {
	          if(pol3X > pol1X)
	          {
	           lewyX = pol1X;
	           lewyY = pol1Y;
	           prawyX = pol3X;
	           prawyY = pol3Y;
	          }
	          else
	          {
	           lewyX = pol3X;
	           lewyY = pol3Y;
	           prawyX = pol1X;
	           prawyY = pol1Y;
	          }
	         }
	         if(pol3Y == pol2Y)
	         {
	          if(pol3X > pol2X)
	          {
	           lewyX = pol2X;
	           lewyY = pol2Y;
	           prawyX = pol3X;
	           prawyY = pol3Y;
	          }
	          else
	          {
	           lewyX = pol3X;
	           lewyY = pol3Y;
	           prawyX = pol2X;
	           prawyY = pol2Y;
	          }
	         }
	         if(pol3Y == pol4Y)
	         {
	          if(pol3X > pol4X)
	          {
	           lewyX = pol4X;
	           lewyY = pol4Y;
	           prawyX = pol3X;
	           prawyY = pol3Y;
	          }
	          else
	          {
	           lewyX = pol3X;
	           lewyY = pol3Y;
	           prawyX = pol4X;
	           prawyY = pol4Y;
	          }
	         }
	         break;
	        case 3:
	         if(pol4Y == pol1Y)
	         {
	          if(pol4X > pol1X)
	          {
	           lewyX = pol1X;
	           lewyY = pol1Y;
	           prawyX = pol4X;
	           prawyY = pol4Y;
	          }
	          else
	          {
	           lewyX = pol4X;
	           lewyY = pol4Y;
	           prawyX = pol1X;
	           prawyY = pol1Y;
	          }
	         }
	         if(pol4Y == pol2Y)
	         {
	          if(pol4X > pol2X)
	          {
	           lewyX = pol2X;
	           lewyY = pol2Y;
	           prawyX = pol4X;
	           prawyY = pol4Y;
	          }
	          else
	          {
	           lewyX = pol4X;
	           lewyY = pol4Y;
	           prawyX = pol2X;
	           prawyY = pol2Y;
	          }
	         }
	         if(pol4Y == pol3Y)
	         {
	          if(pol4X > pol3X)
	          {
	           lewyX = pol3X;
	           lewyY = pol3Y;
	           prawyX = pol4X;
	           prawyY = pol4Y;
	          }
	          else
	          {
	           lewyX = pol4X;
	           lewyY = pol4Y;
	           prawyX = pol3X;
	           prawyY = pol3Y;
	          }
	         }
	         break;
	        default:
	         break;
	    }
	    
	    //sprawdzamy, czy idziemy w dol, czy w gore
	    int kierunek = -1;
	    if(polBY > lewyY)
	    {
	     kierunek = 2;
	    }
	    else
	    {
	     kierunek = 1;
	    }
	    
	    //teraz wstawiamy informacje o kwadracie do rozpoznania do bazy danych
	    int dlugosc = prawyX - lewyX;
	    
	    s.executeUpdate("insert into sc_obszar_do_rozp set " +
	               "id_jednostki = '" + z.getIdJednostki() + "', " +
	               "strona_konf = '" + z.getIdStronyKonfliktu() + "', " +
	               "LDX = " + kwadrat[0] + ", " +
	         "LDY = " + kwadrat[1] + ", " +
	         "PDX = " + kwadrat[2] + ", " +
	         "PDY = " + kwadrat[3] + ", " +
	         "LGX = " + kwadrat[4] + ", " +
	         "LGY = " + kwadrat[5] + ", " +
	         "PGX = " + kwadrat[6] + ", " +
	         "PGY = " + kwadrat[7] + ", " +
	         "LRX = " + lewyX + ", " +
	         "LRY = " + lewyY + ", " +
	         "PRX = " + prawyX + ", " +
	         "PRY = " + prawyY + ", " +
	         "numer_kroku = 0, " + 
	         "numer_kroku_max = " + dlugosc + ", " +
	         "kierunek = " + kierunek + " ON DUPLICATE KEY UPDATE " +
	         "LDX = " + kwadrat[0] + ", " +
	         "LDY = " + kwadrat[1] + ", " +
	         "PDX = " + kwadrat[2] + ", " +
	         "PDY = " + kwadrat[3] + ", " +
	         "LGX = " + kwadrat[4] + ", " +
	         "LGY = " + kwadrat[5] + ", " +
	         "PGX = " + kwadrat[6] + ", " +
	         "PGY = " + kwadrat[7] + ", " +
	         "LRX = " + lewyX + ", " +
	         "LRY = " + lewyY + ", " +
	         "PRX = " + prawyX + ", " +
	         "PRY = " + prawyY + ", " +
	         "numer_kroku = 0, " + 
	         "numer_kroku_max = " + dlugosc + ", " +
	         "kierunek = " + kierunek );
	    
	    System.out.println("Jestem w kwadracie: " + polBX + "; " + polBY);
	    System.out.println("Przechodzê do kwadratu: " + path.get(1).getX() + "; " + path.get(1).getY());
	    
	    //zapisujemy pozycje do ktorej sie udajemy jako aktualna nasza pozycje
	    s.executeUpdate("insert into sc_nasza_poprz_poz set " +
	               "id_jednostki = '" + z.getIdJednostki() + "', " +
	               "strona_konf = '" + z.getIdStronyKonfliktu() + "', " +
	               "pozX_jedn = '" + polBX + "', " +
	               "pozY_jedn = '" + polBX + "', " +
	               "punkt_docelX = '" + endPointX + "', " +
	               "punkt_docelY = '" + endPointY + "' ON DUPLICATE KEY UPDATE " +
	               "pozX_jedn = '" + polBX + "', " +
	               "pozY_jedn = '" + polBX + "', " +
	               "punkt_docelX = '" + endPointX + "', " +
	               "punkt_docelY = '" + endPointY + "'" );
	    
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
          int endPointX = 0;
          int endPointY = 0;
          int nowPointX = 1;
          int nowPointY = 1;
        	
          Class.forName(driver).newInstance();
          con = DriverManager.getConnection(url, username, password);
          Statement s = con.createStatement();
          
          ResultSet rs = s.executeQuery("select * from sc_nasza_poprz_poz z" +
                  " where z.id_jednostki = '" + z.getIdJednostki()  + "'  " +
                  " and z.strona_konf = '" + z.getIdStronyKonfliktu()+ "' ");
         
          if (rs.next()) {
        	  endPointX = rs.getInt("punkt_docelX");
        	  endPointY = rs.getInt("punkt_docelY");
        	  nowPointX = rs.getInt("pozX_jedn");
        	  nowPointY = rs.getInt("pozY_jedn");
          }
          if(endPointX == nowPointX && endPointY == nowPointY){
        	  /*Doszlismy gdzie chcielismy*/
        	  rs = s.executeQuery("select * from sc_obszar_do_rozp z" +
                      " where z.id_jednostki = '" + z.getIdJednostki()  + "'  " +
                      " and z.strona_konf = '" + z.getIdStronyKonfliktu()+ "' ");
        	  if (rs.next()) {
            	  int kierunek = rs.getInt("kierunek");
            	  int nKroku;
            	  if(kierunek == 1){
            		  /*Idziemy do góry*/
	        		  nKroku = rs.getInt("numer_kroku");
	            	  int nKrokuMax = rs.getInt("numer_kroku_max");
	            	  /*Przeszli¶my wszystko, nigdzie nie idziemy dalej*/
	            	  if(nKroku == nKrokuMax)
	            		  return null;	
	        		  int pointLX = rs.getInt("LRX");
	            	  int pointLY = rs.getInt("LRY") + nKroku;
	            	  int pointPX = rs.getInt("PRX");
	            	  int pointPY = rs.getInt("PRY") + nKroku;
	            	  if(pointLX == endPointX &&  pointLY == endPointY){
	            		  /*Jestesmy w lewym dolnym przesuniêtym*/
	                	  int pointPDX = rs.getInt("PDX");
	                	  int pointPDY = rs.getInt("LDY") + nKroku;
	            		  nKroku++;
	                	  endPointX = pointPDX;            		  
	            		  endPointY = pointPDY + nKroku;
	            	  }
	            	  else if(pointPX == endPointX &&  pointPY == endPointY){
	            		  /*Jestesmy w prawym dolnym przesunietym*/
	            		  int pointLDX = rs.getInt("LDX");
	                	  int pointLDY = rs.getInt("PDY") + nKroku;
	                	  nKroku++;
	            		  endPointX = pointLDX;
	            		  endPointY = pointLDY + nKroku;
	            	  }	            	  
            	  }
            	  else{
            		  /*Idziemy w dol*/
            		  nKroku = rs.getInt("numer_kroku");
	            	  int nKrokuMax = rs.getInt("numer_kroku_max");
	            	  /*Przeszli¶my wszystko, nigdzie nie idziemy dalej*/
	            	  if(nKroku == nKrokuMax)
	            		  return null;	
	        		  int pointLX = rs.getInt("LRX");
	            	  int pointLY = rs.getInt("LRY") - nKroku;
	            	  int pointPX = rs.getInt("PRX");
	            	  int pointPY = rs.getInt("PRY") - nKroku;
	            	  if(pointLX == endPointX &&  pointLY == endPointY){
	            		  /*Jestesmy w lewym dolnym przesuniêtym*/
	                	  int pointPDX = rs.getInt("PGX");
	                	  int pointPDY = rs.getInt("LGY") - nKroku;
	            		  nKroku++;
	                	  endPointX = pointPDX;            		  
	            		  endPointY = pointPDY - nKroku;
	            	  }
	            	  else if(pointPX == endPointX &&  pointPY == endPointY){
	            		  /*Jestesmy w prawym dolnym przesunietym*/
	            		  int pointLDX = rs.getInt("LGX");
	                	  int pointLDY = rs.getInt("PGY") - nKroku;
	                	  nKroku++;
	            		  endPointX = pointLDX;
	            		  endPointY = pointLDY - nKroku;
	            	  }            		  
            	  }
            	  List<DirectionXY> path = PathFinder.getInstance().returnWholePath(nowPointX, nowPointY, endPointX, endPointY);
            	  z.setPunktDocelowyPrzemieszczeniaX(path.get(1).getX());
          	      z.setPunktDocelowyPrzemieszczeniaY(path.get(1).getY());
          	      z.setTypZdarzenia(0);	  
          	      
          	      s.executeUpdate("update  sc_obszar_do_rozp set numer_kroku= " +nKroku+
          	    	  " where z.id_jednostki = '" + z.getIdJednostki()  + "'  " +
                      " and z.strona_konf = '" + z.getIdStronyKonfliktu()+ "' ");
              }        	  
          }
          else{
        	  List<DirectionXY> path = PathFinder.getInstance().returnWholePath(nowPointX, nowPointY, endPointX, endPointY);
        	  z.setPunktDocelowyPrzemieszczeniaX(path.get(1).getX());
      	      z.setPunktDocelowyPrzemieszczeniaY(path.get(1).getY());
      	      z.setTypZdarzenia(0);
          }
          s.executeUpdate("insert into sc_nasza_poprz_poz set " +
                  "id_jednostki = '"+z.getIdJednostki()+"', " +
                  "strona_konf = '"+z.getIdStronyKonfliktu()+"', " +
                  "pozX_jedn = '"+z.getPolozenieStartoweJednostkiX()+"', " +
                  "pozY_jedn = '"+z.getPolozenieStartoweJednostkiY()+"', " +
                  "punkt_docelX = '" + endPointX + "', " +
	              "punkt_docelY = '" + endPointY + "' ON DUPLICATE KEY UPDATE " +
                  "pozX_jedn = '"+z.getPolozenieStartoweJednostkiX()+"', " +
                  "pozY_jedn = '"+z.getPolozenieStartoweJednostkiY()+"', " +
                  "punkt_docelX = '" + endPointX + "', " +
	              "punkt_docelY = '" + endPointY + "'");
          
          con.close();
          return new Zdarzenie[] {z};
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
            //return new Zdarzenie[] {z};
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
  public int porownanie(int[] tab, boolean b)
  {
   int i = 0;
   //b = true - sprawdzanie czy wieksze, else czy mniejsze
   if(b)
   {
    int max = tab[0];
    for(i = 1; i < tab.length; i++)
    {
     if(max < tab[i])
      max = tab[i];
    }
    return max;
   }
   else
   {
    int min = tab[0];
    for(i = 1; i < tab.length; i++)
    {
     if(min > tab[i])
      min = tab[i];
    }
    return min;
   }
  }
}


//http://localhost:8080/wsJednostka/services/JednostkaWs
