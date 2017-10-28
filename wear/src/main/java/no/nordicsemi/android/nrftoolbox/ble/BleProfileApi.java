/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrftoolbox.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

public interface BleProfileApi {

	/**
	 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
	 * {@link android.bluetooth.BluetoothGattCallback BluetoothGattCallback} callback before calling
	 * another operation. In order to make BLE operations easier the BleManager allows to enqueue a request
	 * containing all data necessary for a given operation. Requests are performed one after another until the
	 * queue is empty. Use static methods from below to instantiate a request and then enqueue them using {@link #enqueue(Request)}.
	 */
	final class Request {
		enum Type {
			WRITE,
			READ,
			WRITE_DESCRIPTOR,
			READ_DESCRIPTOR,
			ENABLE_NOTIFICATIONS,
			ENABLE_INDICATIONS,
			READ_BATTERY_LEVEL,
			ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
			DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
			ENABLE_SERVICE_CHANGED_INDICATIONS,
		}

		final Type type;
		final BluetoothGattCharacteristic characteristic;
		final BluetoothGattDescriptor descriptor;
		final byte[] value;
		final int writeType;

		private Request(final Type type) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = null;
			this.value = null;
			this.writeType = 0;
		}

		private Request(final Type type, final BluetoothGattCharacteristic characteristic) {
			this.type = type;
			this.characteristic = characteristic;
			this.descriptor = null;
			this.value = null;
			this.writeType = 0;
		}

		private Request(final Type type, final BluetoothGattCharacteristic characteristic, final int writeType, final byte[] value, final int offset, final int length) {
			this.type = type;
			this.characteristic = characteristic;
			this.descriptor = null;
			this.value = copy(value, offset, length);
			this.writeType = writeType;
		}

		private Request(final Type type, final BluetoothGattDescriptor descriptor) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = descriptor;
			this.value = null;
			this.writeType = 0;
		}

		private Request(final Type type, final BluetoothGattDescriptor descriptor, final byte[] value, final int offset, final int length) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = descriptor;
			this.value = copy(value, offset, length);
			this.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
		}

		private static byte[] copy(final byte[] value, final int offset, final int length) {
			if (value == null || offset > value.length)
				return null;
			final int maxLength = Math.min(value.length - offset, length);
			final byte[] copy = new byte[maxLength];
			System.arraycopy(value, offset, copy, 0, maxLength);
			return copy;
		}

		/**
		 * Creates new Read Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have READ property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be read
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newReadRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.READ, characteristic);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param value value to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value) {
			return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), value, 0, value != null ? value.length : 0);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param value value to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param writeType write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int writeType) {
			return new Request(Type.WRITE, characteristic, writeType, value, 0, value != null ? value.length : 0);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param value value to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param offset the offset from which value has to be copied
		 * @param length number of bytes to be copied from the value buffer
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int offset, final int length) {
			return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), value, offset, length);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param value value to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param offset the offset from which value has to be copied
		 * @param length number of bytes to be copied from the value buffer
		 * @param writeType write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int offset, final int length, final int writeType) {
			return new Request(Type.WRITE, characteristic, writeType, value, offset, length);
		}

		/**
		 * Creates new Read Descriptor request. The request will not be executed if given descriptor
		 * is null. After the operation is complete a proper callback will be invoked.
		 * @param descriptor descriptor to be read
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newReadRequest(final BluetoothGattDescriptor descriptor) {
			return new Request(Type.READ_DESCRIPTOR, descriptor);
		}

		/**
		 * Creates new Write Descriptor request. The request will not be executed if given descriptor
		 * is null. After the operation is complete a proper callback will be invoked.
		 * @param descriptor descriptor to be written
		 * @param value value to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] value) {
			return new Request(Type.WRITE_DESCRIPTOR, descriptor, value, 0, value != null ? value.length : 0);
		}

		/**
		 * Creates new Write Descriptor request. The request will not be executed if given descriptor
		 * is null. After the operation is complete a proper callback will be invoked.
		 * @param descriptor descriptor to be written
		 * @param value value to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param offset the offset from which value has to be copied
		 * @param length number of bytes to be copied from the value buffer
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] value, final int offset, final int length) {
			return new Request(Type.WRITE_DESCRIPTOR, descriptor, value, offset, length);
		}

		/**
		 * Creates new Enable Notification request. The request will not be executed if given characteristic
		 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to have notifications enabled
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newEnableNotificationsRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.ENABLE_NOTIFICATIONS, characteristic);
		}

		/**
		 * Creates new Enable Indications request. The request will not be executed if given characteristic
		 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to have indications enabled
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newEnableIndicationsRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.ENABLE_INDICATIONS, characteristic);
		}

		/**
		 * Reads the first found Battery Level characteristic value from the first found Battery Service.
		 * If any of them is not found, or the characteristic does not have the READ property this operation will not execute.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newReadBatteryLevelRequest() {
			return new Request(Type.READ_BATTERY_LEVEL); // the first Battery Level char from the first Battery Service is used
		}

		/**
		 * Enables notifications on the first found Battery Level characteristic from the first found Battery Service.
		 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newEnableBatteryLevelNotificationsRequest() {
			return new Request(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
		}

		/**
		 * Disables notifications on the first found Battery Level characteristic from the first found Battery Service.
		 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newDisableBatteryLevelNotificationsRequest() {
			return new Request(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
		}

		/**
		 * Enables indications on Service Changed characteristic if such exists in the Generic Attribute service.
		 * It is required to enable those notifications on bonded devices on older Android versions to be
		 * informed about attributes changes. Android 7+ (or 6+) handles this automatically and no action is required.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		static Request newEnableServiceChangedIndicationsRequest() {
			return new Request(Type.ENABLE_SERVICE_CHANGED_INDICATIONS); // the only Service Changed char is used (if such exists)
		}
	}

	/**
	 * Returns the context.
	 */
	Context getContext();

	/**
	 * Enables notifications on given characteristic
	 *
	 * @return true is the request has been enqueued
	 */
	boolean enableNotifications(final BluetoothGattCharacteristic characteristic);

	/**
	 * Enables indications on given characteristic
	 *
	 * @return true is the request has been enqueued
	 */
	boolean enableIndications(final BluetoothGattCharacteristic characteristic);

	/**
	 * Sends the read request to the given characteristic.
	 *
	 * @param characteristic the characteristic to read
	 * @return true if request has been enqueued
	 */
	boolean readCharacteristic(final BluetoothGattCharacteristic characteristic);

	/**
	 * Writes the characteristic value to the given characteristic.
	 *
	 * @param characteristic the characteristic to write to
	 * @return true if request has been enqueued
	 */
	boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic);

	/**
	 * Sends the read request to the given descriptor.
	 *
	 * @param descriptor the descriptor to read
	 * @return true if request has been enqueued
	 */
	boolean readDescriptor(final BluetoothGattDescriptor descriptor);

	/**
	 * Writes the descriptor value to the given descriptor.
	 *
	 * @param descriptor the descriptor to write to
	 * @return true if request has been enqueued
	 */
	boolean writeDescriptor(final BluetoothGattDescriptor descriptor);

	/**
	 * Reads the battery level from the device.
	 *
	 * @return true if request has been enqueued
	 */
	boolean readBatteryLevel();

	/**
	 * This method tries to enable notifications on the Battery Level characteristic.
	 *
	 * @param enable <code>true</code> to enable battery notifications, false to disable
	 * @return true if request has been enqueued
	 */
	boolean setBatteryNotifications(final boolean enable);

	/**
	 * Enqueues a new request. The request will be handled immediately if there is no operation in progress,
	 * or automatically after the last enqueued one will finish.
	 * <p>This method should be used to read and write data from the target device as it ensures that the last operation has finished
	 * before a new one will be called.</p>
	 * @param request new request to be performed
	 * @return true if request has been enqueued, false if the device is not connected
	 */
	boolean enqueue(final Request request);
}
