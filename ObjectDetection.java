import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Queue;

import javax.swing.*;
import org.w3c.dom.events.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;

public class ObjectDetection{
	JFrame frame;
	JLayeredPane layer;
    BufferedImage imgOne;
    int height=480;
    int width=640;
	BufferedImage inputImage;
	// HashMap<Integer, ArrayList<ArrayList<Integer>>> islandmap = new HashMap<>();
	HashMap<String, ArrayList<ArrayList<Integer>>> allcoordinates = new HashMap<>();
	ArrayList<String> objectNames = new ArrayList<>();
    
    

   private BufferedImage readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{	

			height = img.getHeight();
			width = img.getWidth();

			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);
			

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					// int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					int pix = ((a << 24) + (r << 16) + (g << 8) + b);

					img.setRGB(x,y,pix);
					ind++;
				}
			}

		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		return img;
	}
	private static class ImagePanel extends JPanel {
        private BufferedImage image;
        public ImagePanel(int width, int height, BufferedImage image) {
            this.image = image;
            image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            repaint();
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // for (int i = 0; i < image.getWidth(); i++) {
            //     for (int j = 0; j < image.getHeight(); j++) {
            //         image.setRGB(i, j, new Color(255, 0, 0, 127).getRGB());
            //     }
            // }
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
	public int findMaxVal(int[] hist){
		int maxFreq = 0;
			int maxHistVal=0;
			for(int i=0;i<hist.length;i++){
				if (maxFreq < hist[i]){
					maxFreq=hist[i];
				}
			}
			for(int i=0;i<hist.length;i++){
				if(hist[i]==maxFreq){
					maxHistVal=i;
				}
			}
		return maxHistVal;
	}
	public int[] makeObjectHistogram(BufferedImage img,int z){
		int[] hueHistogram_object=new int[360];
		int[] satHistogram_object=new int[100];
		int[] valHistogram_object=new int[100];
		int[] YHistogram=new int[257];
		int[] UHistogram=new int[257];
		int[] VHistogram=new int[300];
		double[] hsvArray = new double[3];
		int redCtr=0;
		// int[] redHist = new int[255];
		// int redCtr = 0;
		for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					int clr=img.getRGB(x, y);
					int blue = clr & 0xff;
					int green = (clr & 0xff00) >> 8;
					int red = (clr & 0xff0000) >> 16;

					int rgb = img.getRGB(x, y);
					hsvArray = RGBToHSV(rgb);
					// System.out.println(hsvArray[0]);


					// int red = rgb >> 16 & 0xFF; 
					// int green = (rgb >> 8) & 0xFF;
					// int blue = rgb & 0xFF;
				float[] hsv = Color.RGBtoHSB(red,green,blue, null);
				int hue = (int) (hsv[0]*360);

					if(green==255 && red==0 && blue==0){
						
						continue;
					}else{
						hueHistogram_object[hue]++;

					}
				}
			}
			int maxHueVal = 0;
			int thatSum=0;
			int redSum=0;
			//LOOP FOR CHECKING RED
			
					for(int i=0;i<15;i++)
				{
					redSum+=hueHistogram_object[i];
				}
				for(int i=345;i<360;i++){
					redSum+=hueHistogram_object[i];
				}

				for(int i=45;i<60;i++){
				thatSum=0;
					thatSum+=hueHistogram_object[i];
				}
				if(thatSum>=4000){
						maxHueVal=60;
					}
					else{
						hueHistogram_object[60]=0;
						maxHueVal=findMaxVal(hueHistogram_object);
					}

			
			// printHistogram(hueHistogram_object);

			// printHistogram(YHistogram);
			// printHistogram(UHistogram);
			// printHistogram(VHistogram);
			// printHistogram(valHistogram_object);
			
			return new int[] {findMaxVal(hueHistogram_object),redSum,0,maxHueVal};
			// return [hueHistogram_object;
	}
	// Convert RGB to HSV
    public static double[] RGBToHSV(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

		double h, s, v;

        double min, max, delta;

        min = Math.min(Math.min(r, g), b);
        max = Math.max(Math.max(r, g), b);

        v = max;

        delta = max - min;

        // S
        if (max != 0)
            s = delta / max;
        else {
            s = 0;
            h = -1;
            return new double[] { h, s, v };
        }

        // H
        if (r == max)
            h = (g - b) / delta; // between yellow & magenta
        else if (g == max)
            h = 2 + (b - r) / delta; // between cyan & yellow
        else
            h = 4 + (r - g) / delta; // between magenta & cyan

        h *= 60; // degrees

        if (h < 0)
            h += 360;

        h = h * 1.0;
        s = s * 100.0;
        v = (v / 256.0) * 100.0;

		// System.out.println("(" + r + "," + g +"," + b + ")" + " -> " + h);

		return new double[] { h, s, v };
		
    }
	private int[] computeHistogramAndStoreCoordinatesInput(BufferedImage imgHSV) {
		int[] histogram = new int[360];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = imgHSV.getRGB(x, y);
				float[] hsv = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
				int hue = (int) (hsv[0] * 360);
				histogram[hue]++;
				// inputCoordinates.add(new int[] { x, y, hue });
			}
		}
		int maxValue = Arrays.stream(histogram).max().orElse(1);
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = (int) (histogram[i] * (double) height / maxValue);
		}
		return histogram;
	}
    public void doMath(String[] args){
        //reading original image
		ArrayList<int[]> object_histogram_info = new ArrayList<int[]>();
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		inputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		

		BufferedImage input = readImageRGB(width, height, args[0], inputImage);
		int[] hueHistogram_input = new int[360];
		
		for(int i=1;i<args.length;i++){
				// islandmap.clear();
				HashMap<Integer, ArrayList<ArrayList<Integer>>> islandmap = new HashMap<>();
				BufferedImage img = readImageRGB(width, height, args[i], imgOne);
				int[] hueHistogram_object = new int[360];
				int[] satHistogram_object = new int[100];
				int[] valHistogram_object = new int[100];
				int[] thatArray = new int[4];
				boolean redFlag = false;
				// hueHistogram_object = computeHistogramAndStoreCoordinatesInput(img);
				// printHistogram(hueHistogram_object);
				thatArray= makeObjectHistogram(img,i);
				boolean dotFlag = false;
				String thatObjName = args[i];
				System.out.println(thatObjName);
				// ArrayList<Character> objName = new ArrayList<>();
				// for(int idx=thatObjName.length()-1;idx>=0;idx--){
				// 	// System.out.println(idx);
				// 	System.out.println(thatObjName.charAt(idx));
					
				// 	if(dotFlag){
				// 			System.out.println('2');
				// 		objName.add(thatObjName.charAt(i));
				// 	}
				// 	else if(dotFlag && objName.size()!=0 && thatObjName.charAt(i)=='/'){
				// 		System.out.println('3');

				// 		break;
				// 	}
				// 	else if(thatObjName.charAt(i)=='.'){
				// 		dotFlag=true;
				// 		System.out.println('1');
				// 	}
				// }
				// System.out.println(objName);
				object_histogram_info.add(thatArray);
				if(thatArray[1]>=12000){
					redFlag=true;
				}
				System.out.println(thatArray[3]);
				System.out.println(thatArray[1]);

				//<-----MAKE KERNEL--->
				int w=640;
				int h=480;
				int[][] kernel = new int[h][w];
				for(int r=0;r<h;r++){
					for(int c=0;c<w;c++){
						kernel[r][c]=0;
					}
				}

				//<-----MAIN LOGIC FOR COLOR MATCHING IN INPUT IMAGE----->
				  
			for(int y=0;y<height;y++){
			for(int x = 0; x < width; x++){
				int rgb = input.getRGB(x, y);
					int red = rgb >> 16 & 0xFF; 
					int green = (rgb >> 8) & 0xFF;
					int blue = rgb & 0xFF;
				float[] hsv = Color.RGBtoHSB(red,green,blue, null);


				
					int hue = Math.round(hsv[0] * 359);
					int sat = Math.round(hsv[1] * 99);
					int val = Math.round(hsv[2] * 99);
					
						if(redFlag){
							if((hue>=0 && hue<=10) || (hue>=335 && hue<=359)){
								int[] temp = {x,y};
								kernel[y][x]=1;
								// input.setRGB(x, y, new Color(0, 255, 0, 127).getRGB());
							}
						}
						else if ((hue>=(object_histogram_info.get(i-1))[3]-10) && (hue<=(object_histogram_info.get(i-1))[3]+10))
						{	
						int[] temp = {x,y};
						kernel[y][x]=1;
						// input.setRGB(x, y, new Color(0, 255, 0, 127).getRGB());
						}
					}
				}

				//<------CLUSTERING LOGIC---->
				islandmap = numIslands(kernel,islandmap);
				System.out.println("islandMap:"+islandmap);
				selectbox(thatObjName,islandmap,redFlag);


		}


		
			
		for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					// int color=input.getRGB(x, y);
					// int blue = color & 0xff;
					// int green = (color & 0xff00) >> 8;
					// int red = (color & 0xff0000) >> 16;
					// System.out.println(blue+'-'+green+'-'+red);

					int rgb = input.getRGB(x, y);
					int red = rgb >> 16 & 0xFF; 
					int green = (rgb >> 8) & 0xFF;
					int blue = rgb & 0xFF;
					// System.out.println("blue"+blue);
				float[] hsv = Color.RGBtoHSB(red,green,blue, null);
					int hue = Math.round(hsv[0] * 359);

					hueHistogram_input[hue]++;
				}
			}
			
		
			

		//MAKING A KERNEL
		
		
		

				
			// int no_of_islands = numIslands(kernel);
			// System.out.println("No of islands:"+no_of_islands);
			System.out.println("HELLO WORLD");
			
		JFrame frame = new JFrame();
        int width = 640;
        int height = 480;
		
		
		// selectbox();
		System.out.println("All coordinates:"+allcoordinates);
		for (Map.Entry<String, ArrayList<ArrayList<Integer>>> set :
             allcoordinates.entrySet()) {
 
            // Printing all elements of a Map
            System.out.println("Map"+set.getKey() + " = "
                               + set.getValue().size());
			for(int j = 0;j<set.getValue().size();j++){
				int x1 = set.getValue().get(j).get(0);				
				int x2 = set.getValue().get(j).get(1);				
				int y1 = set.getValue().get(j).get(2);				
				int y2 = set.getValue().get(j).get(3);
				// System.out.println(x1+" bhehegfhjsd"+x2+" "+y1+ " "+y2);	
				Graphics2D g = input.createGraphics();
				g.setStroke(new java.awt.BasicStroke(3));
				g.setColor(Color.RED);
				g.setFont(new Font("Arial", Font.BOLD,14));
				g.drawString(""+extractObjectName(set.getKey()), x1, y2);
				// int[] coordinates = selectbox();
				int boxWidth=x2-x1;
				int boxHeight=y2-y1;
				g.drawRect(x1, y1, boxWidth, boxHeight);
				g.dispose();	
			}
		}
        frame.setSize(width, height);
        // BufferedImage  = new BufferedImage(width, height,
    //         BufferedImage.TYPE_INT_ARGB);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new ImagePanel(width, height, input));
        frame.setVisible(true);
		// System.out.println("COORDDS"+coords);
		// printHistogram(hueHistogram_object);
		

    }
	public static String extractObjectName(String input) {
    
		int lastBackslashIndex = input.lastIndexOf("/");
	 
		int dotRgbIndex = input.indexOf(".rgb");
	
		if (lastBackslashIndex != -1 && dotRgbIndex != -1) {
			String objectName = input.substring(lastBackslashIndex + 1, dotRgbIndex);
			return objectName;
		} else {
			// Return an empty string or handle the case where "\\" or ".rgb" is not found
			return "";
		}
	}

	public void selectbox(String objName,HashMap<Integer, ArrayList<ArrayList<Integer>>> islandmap,boolean redFlag){
		// int[] coords = new int[4];
		for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> set :
             islandmap.entrySet()) {
 
            // Printing all elements of a Map
            // System.out.println(set.getKey() + " = "
            //                    + set.getValue());
			
			if(set.getValue().get(0).size()>=2200){
				System.out.println(set.getKey());
				int minx = Collections.min(set.getValue().get(1));
				int maxx = Collections.max(set.getValue().get(1));
				int miny = Collections.min(set.getValue().get(0));
				int maxy = Collections.max(set.getValue().get(0));
				System.out.println(minx+" "+maxx+" "+miny+" "+maxy);
				// allcoordinates.add(int[]{minx,maxx,miny,maxy});
				ArrayList<Integer> sublist = new ArrayList<>();
				sublist.add(minx);
				sublist.add(maxx);
				sublist.add(miny);
				sublist.add(maxy);
				if(!allcoordinates.containsKey(objName)){
					allcoordinates.put(objName, new ArrayList<ArrayList<Integer>>());
					allcoordinates.get(objName).add(sublist);
				}else{
					allcoordinates.get(objName).add(sublist);
				}
				
				// allcoordinates.get("Object1").add(sublist);
				// allcoordinates.add(sublist);
				// return new int[] {minx,maxx,miny,maxy} ;
			}


        }
		// return coords;
	
	}

	
	public HashMap<Integer, ArrayList<ArrayList<Integer>>> numIslands(int[][] grid,HashMap<Integer, ArrayList<ArrayList<Integer>>> islandmap) {
        int count = 0;
        int[] index = new int[2];
        int row,col;
        for(int i=0;i<grid.length;i++){
            for(int j=0;j<grid[0].length;j++){
                if(grid[i][j] == 1){
                    count++;
                    Queue<int[]> q = new LinkedList<>();
                    q.offer(new int[]{i,j});
                    while(!q.isEmpty()){
                        index = q.poll();
                        row = index[0]; col = index[1];
                        if(row < 0 || row >= grid.length || col < 0 || col >= grid[0].length || grid[row][col] != 1) continue;
                        else {
                            grid[row][col] = 0;
							if(!islandmap.containsKey(count+2)){
								islandmap.put(count+2, new ArrayList<ArrayList<Integer>>());
								ArrayList<Integer> x = new ArrayList<>();
								ArrayList<Integer> y = new ArrayList<>();
								islandmap.get(count+2).add(x);
								islandmap.get(count+2).add(y);
								islandmap.get(count+2).get(0).add(row);
								islandmap.get(count+2).get(1).add(col);
							}else{
								islandmap.get(count+2).get(0).add(row);
								islandmap.get(count+2).get(1).add(col);
							}  
                            q.offer(new int[]{row+1,col});
                            q.offer(new int[]{row-1,col});
                            q.offer(new int[]{row,col+1});
                            q.offer(new int[]{row,col-1});
                        }                      
                
                    }
                }
            }
        }
        return islandmap;
    }
	
	// private static Set<Integer> getNeighbors(int[][] labels, int row, int col) {
    //     Set<Integer> neighbors = new HashSet<>();
    //     int numRows = labels.length;
    //     int numCols = labels[0].length;
	// 	if(labels[row-1][col]==1){
	// 		neighbors.add(labels[row-1][col]);
	// 	}
	// 	if(labels[row][col-1]==1){
	// 		neighbors.add(labels[row][col-1]);
	// 	}
	// 	if(labels[row][col+1]==1){
	// 		neighbors.add(labels[row][col+1]);
	// 	}
	// 	if(labels[row+1][col]==1){
	// 		neighbors.add(labels[row+1][col]);
	// 	}
    //     return neighbors;
    // }

    // private static int find(Map<Integer, Set<Integer>> equivalenceTable, int label) {
    //     while (!equivalenceTable.get(label).contains(label)) {
    //         label = Collections.min(equivalenceTable.get(label));
    //     }
    //     return label;
    // }

	public void showImage(ArrayList<int[]> list){
		for(int i=0;i<list.size();i++){
			System.out.println("COORDS"+list.get(i));
		}
	}
	public static void printHistogram(int[] histogram) {
        for (int i = 0; i < histogram.length; i++) {
            System.out.println("Value " + i + ": " + histogram[i]);
        }
    }
	public static int[] createObjectHistogram(BufferedImage image, char channel) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Initialize the histogram with 256 bins (for 8-bit hue)
        int[] hue_histogram = new int[256];
        int[] saturation_histogram = new int[100];
        int[] value_histogram = new int[100];


        // Define the green chroma background color (adjust these values as needed)
        int greenChromaMinHue = 70; // Minimum hue value for green
        int greenChromaMaxHue = 170; // Maximum hue value for green

        // Iterate through the image pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int hsvColor = image.getRGB(x, y);
                int hue = (hsvColor >> 16) & 0xFF; // Extract the hue value

                // Check if the hue falls within the range of the object (excluding green chroma)
                // if (hue >= greenChromaMinHue && hue <= greenChromaMaxHue) {
                //     continue; // Skip green chroma pixels
                // }

                // Increment the corresponding bin in the histogram for non-green chroma pixels
                hue_histogram[hue]++;
            }
        }

        return hue_histogram;
    }


    public static void main(String[] args) {
        ObjectDetection obj = new ObjectDetection();
        obj.doMath(args);
    }

}