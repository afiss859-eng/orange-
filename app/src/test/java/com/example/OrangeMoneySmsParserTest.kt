package com.example

import com.example.data.model.TransactionType
import com.example.utils.OrangeMoneySmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrangeMoneySmsParserTest {

    @Test
    fun testParseTransferSmsWithReferenceAndBalance() {
        val sms = "Vous avez transfere 15000 FCFA au 22670123456 OUEDRAOGO Moussa. Frais: 150 FCFA. Nouveau solde: 285400 FCFA. ID Transaction: CI240827.1420.A01234."
        val parsed = OrangeMoneySmsParser.parse(sms)

        assertTrue(parsed.isValidOrangeSms)
        assertEquals(TransactionType.TRANSFERT, parsed.detectedType)
        assertEquals(15000.0, parsed.amount ?: 0.0, 0.01)
        assertEquals(150.0, parsed.fee ?: 0.0, 0.01)
        assertEquals("70123456", parsed.clientPhone)
        assertEquals("OUEDRAOGO Moussa", parsed.clientName)
        assertEquals("CI240827.1420.A01234", parsed.reference)
        assertEquals(285400.0, parsed.newBalance ?: 0.0, 0.01)
    }

    @Test
    fun testParseDepotCashInSms() {
        val sms = "Depot de 50000 FCFA effectue avec succes sur le compte 76001122 SAWADOGO Fatou. Frais: 0 FCFA. Solde: 450000 FCFA. Ref: PP240827.0915.B88219"
        val parsed = OrangeMoneySmsParser.parse(sms)

        assertTrue(parsed.isValidOrangeSms)
        assertEquals(TransactionType.DEPOT, parsed.detectedType)
        assertEquals(50000.0, parsed.amount ?: 0.0, 0.01)
        assertEquals(0.0, parsed.fee ?: 0.0, 0.01)
        assertEquals("76001122", parsed.clientPhone)
        assertEquals("SAWADOGO Fatou", parsed.clientName)
        assertEquals("PP240827.0915.B88219", parsed.reference)
        assertEquals(450000.0, parsed.newBalance ?: 0.0, 0.01)
    }

    @Test
    fun testParseRetraitCashOutSms() {
        val sms = "Retrait de 25000 FCFA effectue par le client 75998877 KABORE Karim. Frais: 350 FCFA. Nouveau solde: 125000 FCFA. ID Trans: OM240827.1140.C55432"
        val parsed = OrangeMoneySmsParser.parse(sms)

        assertTrue(parsed.isValidOrangeSms)
        assertEquals(TransactionType.RETRAIT, parsed.detectedType)
        assertEquals(25000.0, parsed.amount ?: 0.0, 0.01)
        assertEquals(350.0, parsed.fee ?: 0.0, 0.01)
        assertEquals("75998877", parsed.clientPhone)
        assertEquals("KABORE Karim", parsed.clientName)
        assertEquals("OM240827.1140.C55432", parsed.reference)
        assertEquals(125000.0, parsed.newBalance ?: 0.0, 0.01)
    }

    @Test
    fun testParseFactureSms() {
        val sms = "Paiement de facture SONABEL de 18450 FCFA effectue avec succes. Reference: FAC240827.1630.9821. Solde restant: 94500 FCFA."
        val parsed = OrangeMoneySmsParser.parse(sms)

        assertTrue(parsed.isValidOrangeSms)
        assertEquals(TransactionType.FACTURE, parsed.detectedType)
        assertEquals(18450.0, parsed.amount ?: 0.0, 0.01)
        assertEquals("FAC240827.1630.9821", parsed.reference)
        assertEquals(94500.0, parsed.newBalance ?: 0.0, 0.01)
    }

    @Test
    fun testParseSoldeConsultationSms() {
        val sms = "Info Orange Money: Le solde de votre compte principal est de 320500 FCFA au 27/08/2026 10:30."
        val parsed = OrangeMoneySmsParser.parse(sms)

        assertTrue(parsed.isValidOrangeSms)
        assertEquals(320500.0, parsed.newBalance ?: 0.0, 0.01)
        assertTrue(parsed.confidenceMessage.isNotBlank())
    }
}
