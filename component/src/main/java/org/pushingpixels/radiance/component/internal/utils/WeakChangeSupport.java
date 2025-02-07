/*
 * Copyright (c) 2005-2022 Radiance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of the copyright holder nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.radiance.component.internal.utils;

import org.pushingpixels.radiance.component.api.common.model.ChangeAware;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WeakChangeSupport {
    // The weak-reference wrapper is not available for usage outside of this class. Otherwise
    // it's hard to enforce the specific pattern usage that the call site holds a strong reference
    // to the original listener, and we are wrapping it as a weak reference here.
    private static class WeakChangeListener implements ChangeListener {
        private WeakReference<ChangeAware> sourceRef;
        private WeakReference<ChangeListener> listenerRef;

        public WeakChangeListener(ChangeAware source, ChangeListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("Cannot wrap a null listener");
            }
            if (source != null) {
                this.sourceRef = new WeakReference<>(source);
            }
            this.listenerRef = new WeakReference<>(listener);
        }

        public ChangeListener getOriginalChangeListener() {
            if (this.listenerRef == null) {
                return null;
            }
            return this.listenerRef.get();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            ChangeListener originalListener = this.listenerRef.get();
            if (originalListener != null) {
                originalListener.stateChanged(e);
            } else {
                // the original is gone - unregister explicitly
                if (this.sourceRef != null) {
                    ChangeAware source = this.sourceRef.get();
                    if (source != null) {
                        source.removeChangeListener(this);
                    }
                }
                this.listenerRef = null;
                this.sourceRef = null;
            }
        }
    }

    private final List<WeakChangeListener> listenerList = new ArrayList<>();
    private final ChangeAware source;

    public WeakChangeSupport(ChangeAware source) {
        this.source = source;
    }

    /**
     * Adds the specified change listener to track changes to the tracked source.
     *
     * @param l Change listener to add.
     * @see #removeChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener l) {
        if (l instanceof WeakChangeListener) {
            throw new IllegalArgumentException("Don't pass an explicitly wrapped listener");
        }
        this.listenerList.add(new WeakChangeListener(this.source, l));
    }

    /**
     * Removes the specified change listener from tracking changes to the tracked source.
     *
     * @param l Change listener to remove.
     * @see #addChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener l) {
        if (l instanceof WeakChangeListener) {
            this.listenerList.remove(l);
            return;
        }

        // In addition to removing the specified change listener, this will also remove all
        // wrappers around listeners that have been reclaimed
        for (int i = this.listenerList.size() - 1; i >= 0; i--) {
            WeakChangeListener current = this.listenerList.get(i);
            ChangeListener original = current.getOriginalChangeListener();
            if ((original == null) || (original == l)) {
                this.listenerList.remove(i);
            }
        }
    }

    /**
     * Notifies all registered listeners that the state of the tracked source has changed.
     */
    public void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (int i = this.listenerList.size() - 1; i >= 0; i--) {
            this.listenerList.get(i).stateChanged(event);
        }
    }
}
