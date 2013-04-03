

package openomr.omr_engine;

import java.util.Iterator;
import java.util.LinkedList;

import org.joone.net.NeuralNet;

import android.graphics.Bitmap;


public class DetectionProcessor
{
	private Bitmap buffImage;
	private StaveDetection staveDetection;
	private Bitmap dupImage;
	
	public DetectionProcessor(Bitmap buffImage, StaveDetection staveDetection, NeuralNet neuralNetwork)
	{
		this.buffImage = buffImage;
		this.staveDetection = staveDetection;
	}

	public void processAll()
	{
		setTopBottomBoundaries();
		setBoundaries();
		dupImage = buffImage.copy(Bitmap.Config.ARGB_8888, true);//(new CopyImage(buffImage)).getCopyOfImage();

		findAllSymbols();
	}

	// This method sets the upper and lower foundaries for each stave found
	private void setTopBottomBoundaries()
	{
		Staves st = staveDetection.getStaveList().get(0);
		st.setStart(st.getTop() - 80);
		int dist = 0;
		for (int i = 0; i < staveDetection.getStaveList().size() - 1; i += 1)
		{
			Staves temp1 = staveDetection.getStaveList().get(i);
			Staves temp2 = staveDetection.getStaveList().get(i + 1);
			dist = (temp2.getTop() - temp1.getBottom()) / 2;
			temp1.setEnd(temp1.getBottom() + dist - 5);
			temp2.setStart(temp2.getTop() - dist + 5);
		}
		st = staveDetection.getStaveList().get(staveDetection.getStaveList().size() - 1);
		st.setEnd(buffImage.getHeight() - 5);
	}

	private void setBoundaries()
	{
		// Locate stave boundaries, measures and L0_Segments
		StaveBoundaries staveBoundaries = new StaveBoundaries(buffImage, staveDetection);
		staveBoundaries.findBoundaries();
		// staveBoundaries.findMeasures();
		staveBoundaries.findGroupsOfNotes();
	}

	private void findAllSymbols()
	{
		int staveCount = 0;

		Iterator it = staveDetection.getStaveInfo();
		while (true)
		{
			if (!it.hasNext())
				break;

			// Process all staves found
			Staves stave = (Staves) it.next();

			staveCount += 1;

//			DrawingTools.drawMeasures(dupImage, stave);

			// Linked list with all L0_Segments
			LinkedList<openomr.omr_engine.L0_Segment> l0_segmentList = stave.getSymbolPos();
			int capacity = l0_segmentList.size();
			//System.out.println("Stave " + staveCount);
			
			//Process each L0 segment present in current stave
			for (int i = 0; i < capacity; i += 1)
			{
				//System.out.printf("*** Segment: %d ***\n", i);
				l0_segmentList.get(i).setParameters(buffImage, dupImage, staveDetection, stave);
				l0_segmentList.get(i).processL0_Segment();
				l0_segmentList.get(i).calculateNoteDuration();
			}
		}
	}

	public Bitmap getDupImage()
	{
		return dupImage;
	}
}
