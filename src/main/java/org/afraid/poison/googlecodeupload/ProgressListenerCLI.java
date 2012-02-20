package org.afraid.poison.googlecodeupload;

/**
 *
 * @author Andreas Schnaiter <rc.poison@gmail.com>
 */
public class ProgressListenerCLI implements ProgressHttpEntity.ProgressListener {

	private final static int TICK_PERCENTAGE=5;
	private final static int LABEL_PERCENTAGE=25;
	private final long totalSize;
	private int lastShown=0;

	public ProgressListenerCLI(final long totalSize) {
		if (0==totalSize) {
			throw new IllegalArgumentException("totalSize must not be 0");
		}
		this.totalSize=totalSize;
	}

	@Override
	public void notifyTransfered(final long bytes) {
		int percentage=(int) Math.round((double) bytes/(double) totalSize*100d);
		//System.err.println(percentage);
		if (0!=bytes) {
			if (Math.round(percentage/LABEL_PERCENTAGE)>Math.round(lastShown/LABEL_PERCENTAGE)) {
				System.err.print(percentage);
				lastShown=percentage;
			} else if (Math.round(percentage/TICK_PERCENTAGE)>Math.round(lastShown/TICK_PERCENTAGE)) {
				System.err.print(".");
				lastShown=percentage;
			}
		}

	}
}
