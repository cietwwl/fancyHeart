// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: message.proto

package com.doteyplay.game.message.proto;

public final class MessageProBuf {
  private MessageProBuf() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rmessage.proto\032\raccount.proto\032\014common.p" +
      "roto\032\ngate.proto\032\nitem.proto\032\tnpc.proto\032" +
      "\nchat.protoB1\n com.doteyplay.game.messag" +
      "e.protoB\rMessageProBuf"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.doteyplay.game.message.proto.AccountProBuf.getDescriptor(),
          com.doteyplay.game.message.proto.CommonProBuf.getDescriptor(),
          com.doteyplay.game.message.proto.GateProBuf.getDescriptor(),
          com.doteyplay.game.message.proto.ItemProBuf.getDescriptor(),
          com.doteyplay.game.message.proto.NpcProBuf.getDescriptor(),
          com.doteyplay.game.message.proto.ChatProBuf.getDescriptor(),
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}