/* Automatically generated nanopb header */
/* Generated by nanopb-0.3.9 at Mon Jan 29 12:41:03 2018. */

#ifndef PB_HOTSPOT_DATA_PB_H_INCLUDED
#define PB_HOTSPOT_DATA_PB_H_INCLUDED
#include <pb.h>

/* @@protoc_insertion_point(includes) */
#if PB_PROTO_HEADER_VERSION != 30
#error Regenerate this file with the current version of nanopb generator.
#endif

#ifdef __cplusplus
extern "C" {
#endif

/* Struct definitions */
typedef struct _Hotspot {
    double latitude;
    double longitude;
    double temperature;
/* @@protoc_insertion_point(struct:Hotspot) */
} Hotspot;

/* Default values for struct fields */

/* Initializer values for message structs */
#define Hotspot_init_default                     {0, 0, 0}
#define Hotspot_init_zero                        {0, 0, 0}

/* Field tags (for use in manual encoding/decoding) */
#define Hotspot_latitude_tag                     1
#define Hotspot_longitude_tag                    2
#define Hotspot_temperature_tag                  3

/* Struct field encoding specification for nanopb */
extern const pb_field_t Hotspot_fields[4];

/* Maximum encoded size of messages (where known) */
#define Hotspot_size                             27

/* Message IDs (where set with "msgid" option) */
#ifdef PB_MSGID

#define HOTSPOT_DATA_MESSAGES \


#endif

#ifdef __cplusplus
} /* extern "C" */
#endif
/* @@protoc_insertion_point(eof) */

#endif
