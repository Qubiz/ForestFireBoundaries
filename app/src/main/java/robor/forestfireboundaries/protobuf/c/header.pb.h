/* Automatically generated nanopb header */
/* Generated by nanopb-0.3.9 at Mon Jan 29 12:40:57 2018. */

#ifndef PB_HEADER_PB_H_INCLUDED
#define PB_HEADER_PB_H_INCLUDED
#include <pb.h>

/* @@protoc_insertion_point(includes) */
#if PB_PROTO_HEADER_VERSION != 30
#error Regenerate this file with the current version of nanopb generator.
#endif

#ifdef __cplusplus
extern "C" {
#endif

/* Struct definitions */
typedef struct _Header {
    uint32_t message_id;
    uint32_t message_length;
/* @@protoc_insertion_point(struct:Header) */
} Header;

/* Default values for struct fields */

/* Initializer values for message structs */
#define Header_init_default                      {0, 0}
#define Header_init_zero                         {0, 0}

/* Field tags (for use in manual encoding/decoding) */
#define Header_message_id_tag                    1
#define Header_message_length_tag                2

/* Struct field encoding specification for nanopb */
extern const pb_field_t Header_fields[3];

/* Maximum encoded size of messages (where known) */
#define Header_size                              10

/* Message IDs (where set with "msgid" option) */
#ifdef PB_MSGID

#define HEADER_MESSAGES \


#endif

#ifdef __cplusplus
} /* extern "C" */
#endif
/* @@protoc_insertion_point(eof) */

#endif