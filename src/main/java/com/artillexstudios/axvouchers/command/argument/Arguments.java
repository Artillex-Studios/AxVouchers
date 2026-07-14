package com.artillexstudios.axvouchers.command.argument;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public enum Arguments {
    INSTANCE;

    public Argument<Voucher> voucher(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            Voucher voucher = Vouchers.parse(info.input());

            if (voucher != null) {
                return voucher;
            } else {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Messages.PREFIX + Messages.VOUCHER_NOT_FOUND, Placeholder.parsed("name", info.input())));
            }
        }).replaceSuggestions(ArgumentSuggestions.strings(info -> Vouchers.getVoucherNames().toArray(new String[0])));
    }
}
