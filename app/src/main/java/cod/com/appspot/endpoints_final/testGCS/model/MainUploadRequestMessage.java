/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2015-01-14 17:53:03 UTC)
 * on 2015-02-10 at 22:32:07 UTC 
 * Modify at your own risk.
 */

package cod.com.appspot.endpoints_final.testGCS.model;

/**
 * Model definition for MainUploadRequestMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the testGCS. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MainUploadRequestMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String artist;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String descr;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String dim;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String filename;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String luogo;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String photo;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String technique;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getArtist() {
    return artist;
  }

  /**
   * @param artist artist or {@code null} for none
   */
  public MainUploadRequestMessage setArtist(java.lang.String artist) {
    this.artist = artist;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDescr() {
    return descr;
  }

  /**
   * @param descr descr or {@code null} for none
   */
  public MainUploadRequestMessage setDescr(java.lang.String descr) {
    this.descr = descr;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDim() {
    return dim;
  }

  /**
   * @param dim dim or {@code null} for none
   */
  public MainUploadRequestMessage setDim(java.lang.String dim) {
    this.dim = dim;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getFilename() {
    return filename;
  }

  /**
   * @param filename filename or {@code null} for none
   */
  public MainUploadRequestMessage setFilename(java.lang.String filename) {
    this.filename = filename;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getLuogo() {
    return luogo;
  }

  /**
   * @param luogo luogo or {@code null} for none
   */
  public MainUploadRequestMessage setLuogo(java.lang.String luogo) {
    this.luogo = luogo;
    return this;
  }

  /**
   * @see #decodePhoto()
   * @return value or {@code null} for none
   */
  public java.lang.String getPhoto() {
    return photo;
  }

  /**

   * @see #getPhoto()
   * @return Base64 decoded value or {@code null} for none
   *
   * @since 1.14
   */
  public byte[] decodePhoto() {
    return com.google.api.client.util.Base64.decodeBase64(photo);
  }

  /**
   * @see # encodePhoto()
   * @param photo photo or {@code null} for none
   */
  public MainUploadRequestMessage setPhoto(java.lang.String photo) {
    this.photo = photo;
    return this;
  }

  /**

   * @see # setPhoto()
   *
   * <p>
   * The value is encoded Base64 or {@code null} for none.
   * </p>
   *
   * @since 1.14
   */
  public MainUploadRequestMessage encodePhoto(byte[] photo) {
    this.photo = com.google.api.client.util.Base64.encodeBase64URLSafeString(photo);
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getTechnique() {
    return technique;
  }

  /**
   * @param technique technique or {@code null} for none
   */
  public MainUploadRequestMessage setTechnique(java.lang.String technique) {
    this.technique = technique;
    return this;
  }

  @Override
  public MainUploadRequestMessage set(String fieldName, Object value) {
    return (MainUploadRequestMessage) super.set(fieldName, value);
  }

  @Override
  public MainUploadRequestMessage clone() {
    return (MainUploadRequestMessage) super.clone();
  }

}
