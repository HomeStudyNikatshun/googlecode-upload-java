package org.afraid.poison.googlecodeupload;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 *
 * @author Andreas Schnaiter <rc.poison@gmail.com>
 */
public class ProgressHttpEntity extends HttpEntityWrapper {

	public static interface ProgressListener {

		void notifyTransfered(long bytes);
	}

	static class ProgressOutputStream extends FilterOutputStream {

		private final ProgressListener listener;
		private transient long transferredBytes;

		ProgressOutputStream(final OutputStream out, final ProgressListener listener) {
			super(out);
			this.listener=listener;
			this.transferredBytes=0;
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			out.write(b, off, len);
			this.transferredBytes+=len;
			this.listener.notifyTransfered(this.transferredBytes);
		}

		@Override
		public void write(final int b) throws IOException {
			out.write(b);
			this.listener.notifyTransfered(++this.transferredBytes);
		}
	}
	private final ProgressListener listener;

	public ProgressHttpEntity(final HttpEntity entity, final ProgressListener listener) {
		super(entity);
		this.listener=listener;
	}

	@Override
	public void writeTo(final OutputStream out) throws IOException {
		this.wrappedEntity.writeTo(new ProgressOutputStream(out, this.listener));
	}
}
