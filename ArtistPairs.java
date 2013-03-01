import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.BitSet;


/**
  Artist Pair Analysis program:
  This program indends to do the following:
  Read N lines from a file where each line is a comma separated list of items. The goal is to find how many items are paired together in at least 50 different lists (lines)
  This program implements three differnet methods for determining this:
  Method 1: HashMap<String,Integer>
            This method reads each line and determines all the actual pairs on that line (m = number of items, so number of pairs is (m(m-1))/2)
	    For each pair that it finds, it cretaes a string to place into the hash map. It then increments the Integer associated with that pair if it already exists
	    Therefore this is keeping a running total of all ACTUAL pairs (not possible pairs) that have been seen and how many times they have been seen.
	    By ordering the strings when they are merged to create the hash kep we don't ahve to worry about a:b and b:a causing double counting
	    This is fairly efficient as the space to store this is J which is the number of unique actual pairings seen and a count of each of those.
	    The time complexity for this is J * (m(m-1))/2 which approaches J * m^2 in the worst case. So essentially this is m^2

  Method 2: HashMap<String,BitSet>
            This method reads each line and identifies all the unique artists on that line. It then creates a hash entry (if one doesnt exist) for each unique artist.
	    For each line that an artist is see on (starting from line 0 in the file) the bit set will be true for that line. For lines where the artist is not seen, the
	    bit set will be false for that line. Therefore as we parse through the file, we see all the unqiue artists and which lines they appear.
	    Once that is complete we need to create all the pairs of artists so that we can identify the pairs that appear at least 50 times. This again is (m(m-1))/2 pairings
	    However, one optimization is to only look at those artists whose cardinality for the bitset is >= 50. Thus we eliminate any artist who hasn't appeared at least 50 times.
	    The space complexity for this is a little worse than the first method because each bitset must be equal in size to the number of input lines, so as the number of input lines grows
	    so does the bitset. if J is the number of lines and m is the number of unique artists, the space complexity is therefore m*n
	    The time complexity for this is (m(m-1))/2 where m is the number of unique artists. So again we are looking at an m^2 algorithm, although the average case m for method 2 will be
	    greater than m for method one (unless ALL possible pairs are seen in the input, then the two m's equal each other)

  Method 3: <HashMap<String,BloomFilter>
            This is essentially the same as method 2 except instead of simply using a bitset I used a 'bloom filter' which is a probabilistic data set. I thought that this would be more 
	    efficient than the two other implementations, however it appears that it is not. 
	    There are two options on the command line when creating the bloom filters. the first is the FalsePositive Probability, the lower the value, the higher the false positives. 
	    The second is the Expected number of elements. This indicates how big the bit set for each artist will be, The code calculates the value of K automatically (number of hash functions)
	    based on these two values.
	    Only when the false positive probability is .5 or greater and the expected number of elements is around 19500 or greater do i get the same results as method 1 or 2. however the runtime 
	    is 10 seconds. However, by decreasing the expected number of elements we can increase the speed at the sake of accuracy. For example if the false positive probability is .5 and the expected
	    number of elements is 100 i get a result of 167 pairs that match. This isn't very far off the real value. This also runs in about 2100 milliseconds. Still not up to the speed of the other
	    two methods. 

  Analysis: Based on the above analysis I would choose method 1 for this dataset, it is reasonably fast, extremely accurate and easy to implement. I am not sure how it will scale, but I think
            the other two methods will scale worse than this one while not offering better results. There probably is another data set or algorithm I havent thought of to handle this kind of processing,
	    but I do not have that at this time.
 **/
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

   public void parseLineIntoBloomFilter(String[] oneLine, int lineIndex, double tolerance, int numElements)
   {
      for ( int i = 0; i < oneLine.length; i++)
      {
         if ( artistBloomMap.get(oneLine[i]) == null )
         {
            artistBloomMap.put(oneLine[i],new BloomFilter<Integer>(tolerance,numElements));
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
      int expectedNumberElements = 1000;
      if (args.length < 2 || args.length > 4 )
      {
	 System.out.println("Incorrect argument count: Usage: java ArtistPairs <artist list file> <pairmap|bitset|bloom> " +
			    "[error tolerance: 0.0->0.999 (default=.5)] [expected num elements (integer) (default=1000)]");
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
      if ( args.length == 4 )
      {
	 tolerance = Double.parseDouble(args[2]);
	 expectedNumberElements = Integer.parseInt(args[3]);
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
		  artistPairer.parseLineIntoBloomFilter(oneLine,count,tolerance,expectedNumberElements);
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
