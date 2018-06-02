#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#include "wb_aes.h"

// kbc tables
#include "kbc_initial_encoding.c"
#include "kbc_typeIA_input_sbox.c"
#include "kbc_typeIAs.c"
#include "kbc_typeIV_IAs.c"
#include "kbc_typeIIs.c"
#include "kbc_typeIV_IIs.c"
#include "kbc_typeIIIs.c"
#include "kbc_typeIV_IIIs.c"
#include "kbc_typeIBs.c"
#include "kbc_typeIV_IBs.c"
#include "kbc_final_decoding.c"
#include "kbc_typeIB_output_sbox_inv.c"

// The input table is packed and of a different version of `mzd_t`,
// this means that we cannot interpret it as a mzd_t struct.
gf2matrix *kbc_to_gf2matrix(uint8_t *kbc_tbl) {

    uint64_t tbl = (uint64_t)kbc_tbl;

    rci_t nrows = *(uint32_t *)tbl;
    rci_t ncols = *(uint32_t *)(tbl + 4);

    gf2matrix *out = malloc(sizeof(*out));

    out->nrows = nrows;
    out->ncols = ncols;
    out->width = *(uint32_t *)(tbl + 8);
    out->rowstride = *(uint32_t *)(tbl + 12);
    out->offset_vector = *(uint32_t *)(tbl + 16);
    out->row_offset = *(uint32_t *)(tbl + 16);
    out->high_bitmask = *(uint16_t *)(tbl + 24);
    out->flags = *(uint8_t *)(tbl + 26);
    out->blockrows_log = *(uint8_t *)(tbl + 27);
    out->blocks = NULL;

    out->rows = malloc(8 * nrows);
    for (uint32_t i=0; i<nrows; i++) {
        out->rows[i] = malloc(8 * ncols);
    }

    for (uint32_t i=0; i<nrows; i++) {
        for (uint32_t j=0; j<ncols; j++) {
            out->rows[i][j] = *(uint64_t *)(tbl + 44 + (i * ncols * 8) + j * 8);
        }
    }

    return out;
}

void free_kbc_g2fmatrix(gf2matrix *m) {
    for (uint32_t i=0; i<m->nrows; i++)  {
        free(m->rows[i]);
    }
    free(m);
}

void get_kbc_initial_encoding(gf2matrix **out) {
    *out = kbc_to_gf2matrix(kbc_initial_encoding);
}

void get_kbc_typeIA_input_sbox(sboxes_8bit_t *out) {
    memcpy(out, kbc_typeIA_input_sbox, sizeof(sboxes_8bit_t));
}

void get_kbc_typeIAs(typeIA_t *out) {
    memcpy(out, kbc_typeIAs, sizeof(typeIA_t));
}

void get_kbc_typeIV_IAs(typeIV_IA_t *out) {
    memcpy(out, kbc_typeIV_IAs, sizeof(typeIV_IA_t));
}

void get_kbc_typeIIs(typeII_t *out) {
    memcpy(out, kbc_typeIIs, sizeof(typeII_t));
}

void get_kbc_typeIV_IIs(typeIV_II_round_t *out) {
    memcpy(out, kbc_typeIV_IIs, sizeof(typeIV_II_round_t) * (NR - 1));
}

void get_kbc_typeIIIs(typeIII_t *out) {
    memcpy(out, kbc_typeIIIs, sizeof(typeIII_t));
}

void get_kbc_typeIV_IIIs(typeIV_III_round_t *out) {
    memcpy(out, kbc_typeIV_IIIs, sizeof(typeIV_III_round_t) * (NR - 1));
}

void get_kbc_typeIBs(typeIB_t *out) {
    memcpy(out, kbc_typeIBs, sizeof(typeIB_t));
}

void get_kbc_typeIV_IBs(typeIV_IB_t *out) {
    memcpy(out, kbc_typeIV_IBs, sizeof(typeIV_IB_t));
}

void get_kbc_final_decoding(gf2matrix **out) {
    *out = kbc_to_gf2matrix(kbc_final_decoding);
}

void get_kbc_typeIB_output_sbox_inv(sboxes_8bit_t *out) {
    memcpy(out, kbc_typeIB_output_sbox_inv, sizeof(sboxes_8bit_t));
}

void kbc_wb_aes128_encrypt(uint8_t *input, uint8_t *output) {
    gf2matrix *initial_encoding;
    gf2matrix *final_decoding;
    typeIA_t typeIAs;
    typeII_t typeIIs;
    typeIII_t typeIIIs;
    typeIB_t typeIBs;
    typeIV_IA_t typeIV_IAs;
    typeIV_IB_t typeIV_IBs;
    typeIV_II_round_t typeIV_IIs[NR - 1];
    typeIV_III_round_t typeIV_IIIs[NR - 1];
    sboxes_8bit_t typeIA_input_sbox;
    sboxes_8bit_t typeIB_output_sbox_inv;

    uint8_t state[4][4];
    _4bit_strip32_t strips32;
    _4bit_strip128_t strips128;

    // Set up KBC state
    get_kbc_initial_encoding(&initial_encoding);
    get_kbc_typeIA_input_sbox(&typeIA_input_sbox);
    get_kbc_typeIAs(&typeIAs);
    get_kbc_typeIV_IAs(&typeIV_IAs);
    get_kbc_typeIIs(&typeIIs);
    get_kbc_typeIV_IIs(&typeIV_IIs[0]);
    get_kbc_typeIIIs(&typeIIIs);
    get_kbc_typeIV_IIIs(&typeIV_IIIs[0]);
    get_kbc_typeIBs(&typeIBs);
    get_kbc_typeIV_IBs(&typeIV_IBs);
    get_kbc_final_decoding(&final_decoding);
    get_kbc_typeIB_output_sbox_inv(&typeIB_output_sbox_inv);

    // Do aes encryption
    do_input(state, input, initial_encoding, typeIA_input_sbox);
    do_typeIA(strips128, state, typeIAs);
    do_typeIV_IA(state, strips128, typeIV_IAs);

    for (int round = 0; round < NR-1; round++) {
        shift_rows(state);
        do_typeII(strips32, state, typeIIs[round]);
        do_typeIV_II(state, strips32, typeIV_IIs[round]);
        do_typeIII(strips32, state, typeIIIs[round]);
        do_typeIV_III(state, strips32, typeIV_IIIs[round]);
    }

    shift_rows(state);
    do_typeIB(strips128, state, typeIBs);
    do_typeIV_IB(state, strips128, typeIV_IBs);

    do_output(output, state, final_decoding, typeIB_output_sbox_inv);

    free_kbc_g2fmatrix(initial_encoding);
    free_kbc_g2fmatrix(final_decoding);
}
