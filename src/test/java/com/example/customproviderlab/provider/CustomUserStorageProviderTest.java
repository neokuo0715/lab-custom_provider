package com.example.customproviderlab.provider;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustomUserStorageProviderTest {

    @InjectMocks
    public CustomUserStorageProvider provider;



//    @Test
    void when_getUserByUserName_then_return_UserModal_with_usrInfo(){
        //given
        RealmModel realm = Mockito.mock(RealmModel.class);
        String username ="use_name";
        //when
        UserModel userModel = provider.getUserByUsername(realm,username);

        //then
        Assertions.assertThat(userModel.getUsername()).isEqualTo("test1");
    }

}
