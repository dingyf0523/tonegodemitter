/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package emitter.influencers;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.util.SafeArrayList;
import java.io.IOException;
import emitter.Interpolation;
import emitter.particle.ParticleData;

/**
 *
 * @author t0neg0d
 */
public class SizeInfluencer implements ParticleInfluencer {
	private SafeArrayList<Vector3f> sizes = new SafeArrayList(Vector3f.class);
	private SafeArrayList<Interpolation> interpolations = new SafeArrayList(Interpolation.class);
	private boolean initialized = false;
	private boolean enabled = true;
	private Vector3f tempV3a = new Vector3f();
	private Vector3f tempV3b = new Vector3f();
	private Vector3f startSize = new Vector3f(.1f,.1f,.1f);
	private Vector3f endSize = new Vector3f(0,0,0);
	private float blend;
	private boolean useRandomSize = false;
	private float randomSizeTolerance = 0.5f;
	private boolean cycle = false;
	private float fixedDuration = 0f;
	
	public void update(ParticleData p, float tpf) {
		if (enabled) {
			p.sizeInterval += tpf;
			if (p.sizeInterval >= p.sizeDuration)
				updateSize(p);
			
			blend = p.sizeInterpolation.apply(p.sizeInterval/p.sizeDuration);
			
			p.size.interpolate(p.startSize, p.endSize, blend);
		}
	}
	
	private void updateSize(ParticleData p) {
		p.sizeIndex++;
	//	if (!cycle) {
			if (p.sizeIndex == sizes.size()-1)
				p.sizeIndex = 0;
	//	} else {
	//		if (p.sizeIndex == sizes.size())
	//			p.sizeIndex = 0;
	//	}
		getNextSizeRange(p);
		p.sizeInterpolation = interpolations.getArray()[p.sizeIndex];
		p.sizeInterval -= p.sizeDuration;
	}
	
	public void initialize(ParticleData p) {
		if (!initialized) {
			if (sizes.isEmpty()) {
				addSize(1f);
				addSize(0f);
			} else if (sizes.size() == 1) {
				setEnabled(false);
			}
			initialized = true;
		}
		
		p.sizeIndex = 0;
		
		p.sizeInterval = 0f;
		p.sizeDuration = (cycle) ? fixedDuration : p.startlife/((float)sizes.size()-1-p.sizeIndex);
		
		getNextSizeRange(p);
		p.sizeInterpolation = interpolations.getArray()[p.sizeIndex];
	}
	
	private void getNextSizeRange(ParticleData p) {
		if (p.sizeIndex == 0) { //endSize.equals(Vector3f.ZERO)) {
			p.startSize.set(sizes.getArray()[p.sizeIndex]);
			if (useRandomSize) {
				tempV3a.set(p.startSize);
				tempV3b.set(tempV3a).multLocal(randomSizeTolerance);
				tempV3a.subtractLocal(tempV3b);
				tempV3b.multLocal(FastMath.nextRandomFloat());
				tempV3a.addLocal(tempV3b);
				p.startSize.set(tempV3a);
			}
		} else {
			p.startSize.set(p.endSize);
		}
		
		if (sizes.size() > 1) {
			if (p.sizeIndex == sizes.size()-1)
				p.endSize.set(sizes.getArray()[0]);
			else
				p.endSize.set(sizes.getArray()[p.sizeIndex+1]);
			if (useRandomSize) {
				tempV3a.set(p.endSize);
				tempV3b.set(tempV3a).multLocal(randomSizeTolerance);
				tempV3a.subtractLocal(tempV3b);
				tempV3b.multLocal(FastMath.nextRandomFloat());
				tempV3a.addLocal(tempV3b);
				p.endSize.set(tempV3a);
			}
		} else {
			p.endSize.set(p.startSize);
		}
		
		p.size.set(p.startSize);
	}
	
	public void reset(ParticleData p) {
		p.size.set(0,0,0);
		p.startSize.set(.1f,.1f,.1f);
		p.endSize.set(0,0,0);
	}
	
	public void addSize(float size) {
		addSize(size, Interpolation.linear);
	}
	
	public void addSize(Vector3f size) {
		addSize(size, Interpolation.linear);
	}
	
	public void addSize(float size, Interpolation interpolation) {
		addSize(new Vector3f(size, size, size), interpolation);
	}
	
	public void addSize(Vector3f size, Interpolation interpolation) {
		this.sizes.add(size.clone());
		this.interpolations.add(interpolation);
	}
	
	public Vector3f[] getSizes() {
		return this.sizes.getArray();
	}
	
	public Interpolation[] getInterpolations() {
		return this.interpolations.getArray();
	}
	
	public void removeSize(int index) {
		this.sizes.remove(index);
		this.interpolations.remove(index);
	}
	
	public void removeAll() {
		this.sizes.clear();
		this.interpolations.clear();
	}
	
	public void setUseRandomSize(boolean useRandomSize) {
		this.useRandomSize = useRandomSize;
	}
	
	public boolean getUseRandomSize() {
		return this.useRandomSize;
	}
	
	public void setRandomSizeTolerance(float randomSizeTolerance) {
		this.randomSizeTolerance = randomSizeTolerance;
	}
	
	public float getRandomSizeTolerance() {
		return this.randomSizeTolerance;
	}
	
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
        oc.write(startSize, "startColor", new Vector3f(1,1,1));
        oc.write(endSize, "endColor", new Vector3f(0,0,0));
	}

	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		startSize = (Vector3f) ic.readSavable("startColor", new Vector3f(1,1,1));
		endSize = (Vector3f) ic.readSavable("endColor", new Vector3f(0,0,0));
	}

	@Override
	public ParticleInfluencer clone() {
		try {
			SizeInfluencer clone = (SizeInfluencer) super.clone();
			clone.sizes.addAll(sizes);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
	
	/**
	 * Animated texture should cycle and use the provided duration between frames (0 diables cycling)
	 * @param fixedDuration duration between frame updates
	 */
	public void setFixedDuration(float fixedDuration) {
		if (fixedDuration != 0) {
			this.cycle = true;
			this.fixedDuration = fixedDuration;
		} else {
			this.cycle = false;
			this.fixedDuration = 0;
		}
	}
	/**
	 * Returns the current duration used between frames for cycled animation
	 * @return 
	 */
	public float getFixedDuration() { return this.fixedDuration; }

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Class getInfluencerClass() {
		return SizeInfluencer.class;
	}
}
