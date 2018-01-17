// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: hotspot-data.proto

package robor.forestfireboundaries.protobuf;

public final class HotspotDataProtos {
  private HotspotDataProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface HotspotOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Hotspot)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>double latitude = 1;</code>
     */
    double getLatitude();

    /**
     * <code>double longitude = 2;</code>
     */
    double getLongitude();

    /**
     * <code>double temperature = 3;</code>
     */
    double getTemperature();
  }
  /**
   * Protobuf type {@code Hotspot}
   */
  public  static final class Hotspot extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:Hotspot)
      HotspotOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use Hotspot.newBuilder() to construct.
    private Hotspot(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private Hotspot() {
      latitude_ = 0D;
      longitude_ = 0D;
      temperature_ = 0D;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Hotspot(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 9: {

              latitude_ = input.readDouble();
              break;
            }
            case 17: {

              longitude_ = input.readDouble();
              break;
            }
            case 25: {

              temperature_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return robor.forestfireboundaries.protobuf.HotspotDataProtos.internal_static_Hotspot_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return robor.forestfireboundaries.protobuf.HotspotDataProtos.internal_static_Hotspot_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.class, robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.Builder.class);
    }

    public static final int LATITUDE_FIELD_NUMBER = 1;
    private double latitude_;
    /**
     * <code>double latitude = 1;</code>
     */
    public double getLatitude() {
      return latitude_;
    }

    public static final int LONGITUDE_FIELD_NUMBER = 2;
    private double longitude_;
    /**
     * <code>double longitude = 2;</code>
     */
    public double getLongitude() {
      return longitude_;
    }

    public static final int TEMPERATURE_FIELD_NUMBER = 3;
    private double temperature_;
    /**
     * <code>double temperature = 3;</code>
     */
    public double getTemperature() {
      return temperature_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (latitude_ != 0D) {
        output.writeDouble(1, latitude_);
      }
      if (longitude_ != 0D) {
        output.writeDouble(2, longitude_);
      }
      if (temperature_ != 0D) {
        output.writeDouble(3, temperature_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (latitude_ != 0D) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(1, latitude_);
      }
      if (longitude_ != 0D) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(2, longitude_);
      }
      if (temperature_ != 0D) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(3, temperature_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot)) {
        return super.equals(obj);
      }
      robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot other = (robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot) obj;

      boolean result = true;
      result = result && (
          java.lang.Double.doubleToLongBits(getLatitude())
          == java.lang.Double.doubleToLongBits(
              other.getLatitude()));
      result = result && (
          java.lang.Double.doubleToLongBits(getLongitude())
          == java.lang.Double.doubleToLongBits(
              other.getLongitude()));
      result = result && (
          java.lang.Double.doubleToLongBits(getTemperature())
          == java.lang.Double.doubleToLongBits(
              other.getTemperature()));
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + LATITUDE_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          java.lang.Double.doubleToLongBits(getLatitude()));
      hash = (37 * hash) + LONGITUDE_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          java.lang.Double.doubleToLongBits(getLongitude()));
      hash = (37 * hash) + TEMPERATURE_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          java.lang.Double.doubleToLongBits(getTemperature()));
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Hotspot}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Hotspot)
        robor.forestfireboundaries.protobuf.HotspotDataProtos.HotspotOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return robor.forestfireboundaries.protobuf.HotspotDataProtos.internal_static_Hotspot_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return robor.forestfireboundaries.protobuf.HotspotDataProtos.internal_static_Hotspot_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.class, robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.Builder.class);
      }

      // Construct using robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        latitude_ = 0D;

        longitude_ = 0D;

        temperature_ = 0D;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return robor.forestfireboundaries.protobuf.HotspotDataProtos.internal_static_Hotspot_descriptor;
      }

      public robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot getDefaultInstanceForType() {
        return robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.getDefaultInstance();
      }

      public robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot build() {
        robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot buildPartial() {
        robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot result = new robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot(this);
        result.latitude_ = latitude_;
        result.longitude_ = longitude_;
        result.temperature_ = temperature_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot) {
          return mergeFrom((robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot other) {
        if (other == robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot.getDefaultInstance()) return this;
        if (other.getLatitude() != 0D) {
          setLatitude(other.getLatitude());
        }
        if (other.getLongitude() != 0D) {
          setLongitude(other.getLongitude());
        }
        if (other.getTemperature() != 0D) {
          setTemperature(other.getTemperature());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private double latitude_ ;
      /**
       * <code>double latitude = 1;</code>
       */
      public double getLatitude() {
        return latitude_;
      }
      /**
       * <code>double latitude = 1;</code>
       */
      public Builder setLatitude(double value) {
        
        latitude_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>double latitude = 1;</code>
       */
      public Builder clearLatitude() {
        
        latitude_ = 0D;
        onChanged();
        return this;
      }

      private double longitude_ ;
      /**
       * <code>double longitude = 2;</code>
       */
      public double getLongitude() {
        return longitude_;
      }
      /**
       * <code>double longitude = 2;</code>
       */
      public Builder setLongitude(double value) {
        
        longitude_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>double longitude = 2;</code>
       */
      public Builder clearLongitude() {
        
        longitude_ = 0D;
        onChanged();
        return this;
      }

      private double temperature_ ;
      /**
       * <code>double temperature = 3;</code>
       */
      public double getTemperature() {
        return temperature_;
      }
      /**
       * <code>double temperature = 3;</code>
       */
      public Builder setTemperature(double value) {
        
        temperature_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>double temperature = 3;</code>
       */
      public Builder clearTemperature() {
        
        temperature_ = 0D;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:Hotspot)
    }

    // @@protoc_insertion_point(class_scope:Hotspot)
    private static final robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot();
    }

    public static robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Hotspot>
        PARSER = new com.google.protobuf.AbstractParser<Hotspot>() {
      public Hotspot parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Hotspot(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Hotspot> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<Hotspot> getParserForType() {
      return PARSER;
    }

    public robor.forestfireboundaries.protobuf.HotspotDataProtos.Hotspot getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Hotspot_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Hotspot_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\022hotspot-data.proto\"C\n\007Hotspot\022\020\n\010latit" +
      "ude\030\001 \001(\001\022\021\n\tlongitude\030\002 \001(\001\022\023\n\013temperat" +
      "ure\030\003 \001(\001B8\n#robor.forestfireboundaries." +
      "protobufB\021HotspotDataProtosb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_Hotspot_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Hotspot_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Hotspot_descriptor,
        new java.lang.String[] { "Latitude", "Longitude", "Temperature", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
