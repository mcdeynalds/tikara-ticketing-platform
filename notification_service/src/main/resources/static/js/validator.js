/**
 * TIKARA - Centralized Input Validation Library
 * Provides real-world validators for NIK, phone, email, bank account, and more.
 * Usage: window.TikaraValidator.validateNik('3201...') => { valid: bool, message: string }
 */
(function(window) {
    'use strict';

    const Validator = {};

    // --- NIK (Nomor Induk Kependudukan) ---------------------------------------
    // 16 digits; first 2 = province (01-96), 3-4 = city, 5-6 = district
    // 7-12 = date (for female, day+40), 13-16 = sequence
    Validator.validateNik = function(nik) {
        const clean = nik.replace(/\D/g, '');
        if (!clean) return { valid: false, message: 'NIK tidak boleh kosong' };
        if (clean.length !== 16) return { valid: false, message: 'NIK harus 16 digit angka' };
        if (!/^\d{16}$/.test(clean)) return { valid: false, message: 'NIK hanya boleh berisi angka' };

        const province = parseInt(clean.substring(0, 2));
        if (province < 11 || province > 96) return { valid: false, message: 'Kode provinsi pada NIK tidak valid' };

        // Validate birth date component (digits 7-12)
        const dd = parseInt(clean.substring(6, 8)); // day (females: day+40)
        const mm = parseInt(clean.substring(8, 10));
        const actualDay = dd > 40 ? dd - 40 : dd;

        if (actualDay < 1 || actualDay > 31) return { valid: false, message: 'Tanggal lahir pada NIK tidak valid' };
        if (mm < 1 || mm > 12) return { valid: false, message: 'Bulan lahir pada NIK tidak valid' };

        return { valid: true, message: 'NIK valid' };
    };

    // --- Phone Number (Indonesian) --------------------------------------------
    // Format: 08xx, +628xx, 628xx; length 10-13 digits
    Validator.validatePhone = function(phone) {
        const clean = phone.replace(/[\s\-\.]/g, '');
        if (!clean) return { valid: false, message: 'Nomor telepon tidak boleh kosong' };

        const normalized = clean.startsWith('+62') ? '0' + clean.slice(3)
                         : clean.startsWith('62')  ? '0' + clean.slice(2)
                         : clean;

        if (!/^08[1-9]\d{7,10}$/.test(normalized)) {
            return { valid: false, message: 'Format nomor tidak valid. Contoh: 081234567890' };
        }
        if (normalized.length < 10 || normalized.length > 13) {
            return { valid: false, message: 'Nomor telepon harus 10-13 digit' };
        }
        return { valid: true, message: 'Nomor telepon valid', normalized: normalized };
    };

    // --- Email ----------------------------------------------------------------
    Validator.validateEmail = function(email) {
        if (!email || !email.trim()) return { valid: false, message: 'Email tidak boleh kosong' };
        const re = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}$/;
        if (!re.test(email.trim())) return { valid: false, message: 'Format email tidak valid. Contoh: nama@domain.com' };
        const parts = email.split('@');
        if (parts[0].length > 64) return { valid: false, message: 'Bagian lokal email terlalu panjang' };
        if (parts[1] && parts[1].length > 255) return { valid: false, message: 'Domain email terlalu panjang' };
        return { valid: true, message: 'Email valid' };
    };

    // --- Password -------------------------------------------------------------
    Validator.validatePassword = function(pass) {
        if (!pass) return { valid: false, message: 'Kata sandi tidak boleh kosong', strength: 0 };
        const checks = {
            length:  pass.length >= 8,
            capital: /[A-Z]/.test(pass),
            lower:   /[a-z]/.test(pass),
            number:  /[0-9]/.test(pass),
            symbol:  /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pass),
        };
        const score = Object.values(checks).filter(Boolean).length;
        if (!checks.length)   return { valid: false, checks, strength: score, message: 'Kata sandi minimal 8 karakter' };
        if (!checks.capital)  return { valid: false, checks, strength: score, message: 'Kata sandi harus diawali huruf kapital' };
        if (!checks.number)   return { valid: false, checks, strength: score, message: 'Kata sandi harus memiliki minimal 1 angka' };
        if (!checks.symbol)   return { valid: false, checks, strength: score, message: 'Kata sandi harus memiliki minimal 1 simbol (!, @, #, ...)' };
        return { valid: true, checks, strength: score, message: 'Kata sandi kuat' };
    };

    // --- Bank Account Number --------------------------------------------------
    // Indonesian bank accounts: 6-16 digits depending on bank
    Validator.validateBankAccount = function(accountNum, bankCode) {
        const clean = accountNum.replace(/\D/g, '');
        if (!clean) return { valid: false, message: 'Nomor rekening tidak boleh kosong' };
        if (!/^\d+$/.test(clean)) return { valid: false, message: 'Nomor rekening hanya boleh berisi angka' };

        // Bank-specific length rules
        const bankRules = {
            'BCA':     { min: 10, max: 10, label: 'BCA (10 digit)' },
            'MANDIRI': { min: 13, max: 13, label: 'Mandiri (13 digit)' },
            'BRI':     { min: 15, max: 15, label: 'BRI (15 digit)' },
            'BNI':     { min: 10, max: 10, label: 'BNI (10 digit)' },
            'BTN':     { min: 13, max: 14, label: 'BTN (13-14 digit)' },
            'CIMB':    { min: 13, max: 14, label: 'CIMB Niaga (13-14 digit)' },
            'DANAMON': { min: 16, max: 16, label: 'Danamon (16 digit)' },
        };

        if (bankCode && bankRules[bankCode.toUpperCase()]) {
            const rule = bankRules[bankCode.toUpperCase()];
            if (clean.length < rule.min || clean.length > rule.max) {
                return { valid: false, message: 'Nomor rekening ' + rule.label };
            }
        } else {
            // Generic: 6-16 digits
            if (clean.length < 6 || clean.length > 16) {
                return { valid: false, message: 'Nomor rekening harus 6-16 digit' };
            }
        }
        return { valid: true, message: 'Nomor rekening valid', normalized: clean };
    };

    // --- Full Name ------------------------------------------------------------
    Validator.validateFullName = function(name) {
        if (!name || !name.trim()) return { valid: false, message: 'Nama tidak boleh kosong' };
        const clean = name.trim();
        if (clean.length < 3)   return { valid: false, message: 'Nama minimal 3 karakter' };
        if (clean.length > 60) return { valid: false, message: 'Nama maksimal 60 karakter' };
        if (/\d/.test(clean))   return { valid: false, message: 'Nama tidak boleh mengandung angka' };
        if (/[!@#$%^&*()_+=\[\]{};:"\\|<>\/?]/.test(clean)) {
            return { valid: false, message: 'Nama tidak boleh mengandung simbol khusus' };
        }
        return { valid: true, message: 'Nama valid' };
    };

    // --- Promo Code -----------------------------------------------------------
    Validator.validatePromoCode = function(code) {
        if (!code || !code.trim()) return { valid: false, message: 'Kode promo tidak boleh kosong' };
        const clean = code.trim().toUpperCase();
        if (clean.length < 3)  return { valid: false, message: 'Kode promo minimal 3 karakter' };
        if (clean.length > 20) return { valid: false, message: 'Kode promo maksimal 20 karakter' };
        if (!/^[A-Z0-9_\-]+$/.test(clean)) return { valid: false, message: 'Kode promo hanya boleh huruf, angka, atau tanda - dan _' };
        return { valid: true, message: 'Format kode promo valid', normalized: clean };
    };

    // --- UI Helpers -----------------------------------------------------------
    /**
     * Apply validation state to an input element.
     * @param {HTMLElement} input - The input element
     * @param {Object} result - Validation result {valid, message}
     * @param {string} errorElId - ID of error message element
     */
    Validator.applyState = function(input, result, errorElId) {
        if (!input) return;
        input.classList.remove('input-valid', 'input-error');
        input.classList.add(result.valid ? 'input-valid' : 'input-error');

        if (errorElId) {
            const errEl = document.getElementById(errorElId);
            if (errEl) {
                errEl.textContent = result.valid ? '' : result.message;
                errEl.style.display = result.valid ? 'none' : 'block';
            }
        }
        return result.valid;
    };

    /**
     * Format NIK input (adds spaces every 4 digits for readability)
     */
    Validator.formatNikInput = function(input) {
        let val = input.value.replace(/\D/g,'').substring(0,16);
        input.value = val; // keep raw digits
        return val;
    };

    /**
     * Format phone number input
     */
    Validator.formatPhoneInput = function(input) {
        let val = input.value.replace(/[^\d+]/g,'');
        input.value = val;
        return val;
    };

    // Export
    window.TikaraValidator = Validator;

})(window);

