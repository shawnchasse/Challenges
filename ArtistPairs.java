import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.BitSet;



public class ArtistPairs
{
   private HashMap<String,Integer> pairMap = new HashMap<String,Integer>();
   private HashMap<String,BitSet> artistMap = new HashMap<String,BitSet>();								 
   private HashMap<String,BloomFilter<Integer>> artistBloomMap = new HashMap<String,BloomFilter<Integer>>();
   private int numberIndices = 1000;
   private double bfErrTolerance = .8;
   public ArtistPairs()
   {
      
   }

   public ArtistPairs(int bitSetSize)
   {
      numberIndices = bitSetSize;
   }

   public void parseLineIntoBloomFilter(String[] oneLine, int lineIndex, double tolerance)
   {
      for ( int i = 0; i < oneLine.length; i++)
      {
         if ( artistBloomMap.get(oneLine[i]) == null )
         {
            artistBloomMap.put(oneLine[i],new BloomFilter<Integer>(tolerance,numberIndices));
         }
	 artistBloomMap.get(oneLine[i]).add(lineIndex);
      } 
      
   }

   public void printPairsInBloomMap()
   {
      Object[] artists = artistBloomMap.keySet().toArray();
      int numberQualifiedPairs = 0;
      System.out.println("Total number of unique artists: " + artists.length);
      for ( int i = 0; i < artists.length; i++ )
      {
         for (int j = i+1; j < artists.length; j++ )
         {

            String a = (String)artists[i];
            String b = (String)artists[j];
            if ( artistBloomMap.get(a).getBitSet().cardinality() >= 50 && artistBloomMap.get(b).getBitSet().cardinality() >= 50 )
            {
               BitSet result = (BitSet)(artistBloomMap.get(a).getBitSet().clone());
               result.and(artistBloomMap.get(b).getBitSet());
               if ( result.cardinality() >= 50 )
               {

                  if ( (a).compareTo(b) > 0 )
                  {
		     System.out.println(b + "," + a);
                  }
                  else
                  {
                     System.out.println(a + "," + b);
                  }
		  numberQualifiedPairs++;
               }
            }
         }
      }
      System.out.println("Total number qualified pairs: " + numberQualifiedPairs );
   }


   public void parseLineIntoBitSetMap(String[] oneLine, int lineIndex)
   {
      for ( int i = 0; i < oneLine.length; i++)
      {
	 if ( artistMap.get(oneLine[i]) == null )
	 {
	    artistMap.put(oneLine[i],new BitSet(numberIndices));
	 }
	 artistMap.get(oneLine[i]).set(lineIndex);
      }
   }

   public void printPairsInBitSetMap()
   {
      Object[] artists = artistMap.keySet().toArray();
      System.out.println("Total number of unique artists: " + artists.length);
      int numberQualifiedPairs = 0;
      for ( int i = 0; i < artists.length; i++ )
      {
	 for (int j = i+1; j < artists.length; j++ )
	 {
	    
	    String a = (String)artists[i];
	    String b = (String)artists[j];
	    if ( artistMap.get(a).cardinality() >= 50 && artistMap.get(b).cardinality() >= 50 )
	    {
	       BitSet result = (BitSet)(artistMap.get(a).clone());
	       result.and(artistMap.get(b));
	       if ( result.cardinality() >= 50 )
	       {
	       
		  if ( (a).compareTo(b) > 0 )
		  {
		     System.out.println(b + "," + a);
		  }
		  else
		  {
		     System.out.println(a + "," + b);
		  }
                  numberQualifiedPairs++;
	       }

	    }
	 }
      }
      System.out.println("Total number of qualified pairs: " + numberQualifiedPairs );
   }

   public void printPairsInPairMap()
   {
      System.out.println("Total unique pairings: " + pairMap.size());
      int numberQualifiedPairs = 0;
      for ( String key : pairMap.keySet() )
      {
         if ( pairMap.get(key) >= Integer.valueOf(50) )
         {
            System.out.println(key);
	    numberQualifiedPairs++;
         }
      }
      System.out.println("Total number of qualifiedPairs: " + numberQualifiedPairs);
   }

   public void parseLineIntoPairMap(String[] oneLine)
   {

      for ( int i = 0; i < oneLine.length; i++ )
      {
	 for ( int j = i+1; j < oneLine.length; j++ )
	 {
	    String strPair = "";
	    if ( oneLine[i].compareTo(oneLine[j]) > 0 )
	    {
	       strPair = oneLine[j] + "," + oneLine[i];

	    }
	    else
	    {
	       strPair = oneLine[i] + "," + oneLine[j];
	    }
	    if( pairMap.get(strPair) != null )
	    {
	       pairMap.put(strPair,pairMap.get(strPair) +1);
	    }
	    else
	    {
	       pairMap.put(strPair,Integer.valueOf(1));
	    }
	 }
      }
   }

   public static void main (String args[])
   {
      String fileName = args[0];
      String method = "";
      double tolerance = 0.5;
      if (args.length < 2 || args.length > 3 )
      {
	 System.out.println("Incorrect argument count: Usage: java ArtistPairs <artist list file> <pairmap|bitset|bloom> [error tolerance: 0.0->1.0]");
	 System.exit(1);
      }
      if ( args.length > 1 )
      {
	 method = args[1];
      }
      if ( !(method.equals("bloom") || method.equals("pairmap") || method.equals("bitset")) )
      {
	 System.out.println("Incorrect argument count: Usage: java ArtistPairs <artist list file> <pairmap|bitset|bloom> [error tolerance: 0.0->1.0]");
	 System.exit(1);
      }
      if ( args.length == 3 )
      {
	 tolerance = Double.parseDouble(args[2]);
      }
      
      long start = System.currentTimeMillis();
      ArtistPairs artistPairer = new ArtistPairs(1000);
      File artistList = new File(fileName);
      if ( artistList.exists() )
      {
	 BufferedReader in = null;
	 try
	 {
	    FileReader fReader = new FileReader(artistList);
	    in = new BufferedReader(fReader);
	    
	    int count = 0;
	    String curLine = "";
	    while ( (curLine = in.readLine()) != null )
	    {
	       
	       String[] oneLine = curLine.split(",");
	       if ( method.equals("pairmap") )
	       {
		  artistPairer.parseLineIntoPairMap(oneLine);
	       }
	       else if ( method.equals("bitset") )
	       {
		  artistPairer.parseLineIntoBitSetMap(oneLine,count);
	       }
	       else if ( method.equals("bloom") )
	       {
		  artistPairer.parseLineIntoBloomFilter(oneLine,count,tolerance);
	       }
	       else
	       {
		  System.out.println("Unable to find parsing algorithm for passed in method value: " + method );
		  break;
	       }

	       count++;
	    }
	 }
	 catch ( java.io.IOException ioe )
	 {
	    System.out.println("IO Exception caught while reading from file: " + ioe);
	 }
	 finally
	 {
	    try
	    {
	       in.close();
	    }
	    catch ( java.io.IOException ioe )
	    {
	       System.out.println("IOException during file close operation" );
	    }
	 }
	 if ( method.equals("pairmap"))
	 {
	    artistPairer.printPairsInPairMap();
	 }
	 else if ( method.equals("bitset") )
	 {
	    artistPairer.printPairsInBitSetMap();
	 }
	 else if ( method.equals("bloom") )
	 {
	    artistPairer.printPairsInBloomMap();
	 }
	 long stop = System.currentTimeMillis();
	 System.out.println("Completed in " + (stop-start) + "ms");
      }
   }
}
