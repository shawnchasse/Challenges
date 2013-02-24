import java.util.*;

public class AreaRect
{
   public class Rectangle
   {
      int start = 0;
      int end = 0;
      int height = 0;
      
      public Rectangle( int s, int e, int h )
      {
	 start = s;
	 end = e;
	 height = h;
      }
      
      public int getHeight()
      {
	 return height;
      }
      public int getStart()
      {
	 return start;
      }
      public int getEnd()
      {
	 return end;
      }
   }

   int maxEnd = 0;
   int maxHeight = 0;
   int[] heightMap = null;
   java.util.LinkedList<Rectangle> rectList = new java.util.LinkedList<Rectangle>();
   public AreaRect()
   {
   }

   public void parseTriplets(String[] triplets)
   {
      for ( String triplet : triplets )
      {
         String[] parts = triplet.split(",");
         if ( parts.length == 3 )
         {
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            int height = Integer.parseInt(parts[2]);
            Rectangle r = new Rectangle(start, end, height);
	    rectList.add(r);
            if ( maxEnd < end )
            {
               maxEnd = end;
            }
            if ( maxHeight < height )
            {
               maxHeight = height;
            }
         }
         else
         {
            System.out.println("Error parsing input, triplet was malformed: " + triplet );
         }
      }
   }

   public void createHeightMap()
   {
      int area = 0;
      heightMap = new int[maxEnd+1];
      for ( Rectangle r : rectList )
      {
	 for ( int i = r.getStart(); i <= r.getEnd(); i++)
	 {
	    if ( r.getHeight() > heightMap[i] )
	    {
	       area += r.getHeight() - heightMap[i];
	       heightMap[i] = r.getHeight();
	    }
	 }
      }
      System.out.println("Total area is " + area );
   }

   public void printHeightMap()
   {
      int curHeight = maxHeight;
      while (curHeight >= 0 )
      {
	 for ( int i = 0; i < heightMap.length; i++ )
	 {
	    if ( heightMap[i] != 0 && heightMap[i] >= curHeight )
	    {
	       System.out.print("#");
	    }
	    else
	    {
	       System.out.print(" ");
	    }
	 }
	 curHeight--;
	 System.out.print("\n");
      }
   }

   public static void main (String args[])
   {
      // input is 1 line of text with comma separated triplets followed by spaces
      // ie: 1,3,4 4,3,5 6,5,7 2,3,5 ...
      Scanner in = new Scanner(System.in);
      String input = "";
      if( in.hasNextLine() )
      {
	 input = in.nextLine(); 
      }
      
      AreaRect ar = new AreaRect();
      
      ar.parseTriplets(input.split(" "));
      ar.createHeightMap();
      ar.printHeightMap();

      
   }

}
