$(function() {
    $('#submit').click(function(e) {
        let form = $('#form');
        form.validate({
            rules: {
              'file': 'required',
              'email': {
                  required: true,
                  email: true
              }
            },
            messages: {
              'file': 'Välj en Excel-fil att importera',
              'email': 'Ange en e-postadress att skicka resultatet till'
            },
            errorClass: 'text-danger'
        });

        if (form.valid()) {
            e.preventDefault();

            let form = document.getElementById('form');
            let formData = new FormData(form);

            $('#spinner').css('display', 'inline-block');
            $('#submit').attr('disabled', 'disabled');
            $('#file').attr('disabled', 'disabled');
            $('#email').attr('disabled', 'disabled');

            $.ajax({
               url: '/import',
               type: 'POST',
               data: formData,
               contentType: false,
               cache: false,
               processData: false,
               dataType: 'json',
               success: function (data) {
                   alert('Import klar!\n\n' +
                         'Totalt ' + data.total + ' post(er):\n\n' +
                         '    ' + data.successful + ' lyckad(e)\n' +
                         '    ' + data.failed + ' misslyckad(e)'
                   );
               },
               error: function (xhr, status, err) {
                   alert(xhr.responseText);
               },
               complete: function (xhr, data) {
                   $('#spinner').css('display', 'none');
                   $('#submit').removeAttr('disabled');
                   $('#file').removeAttr('disabled');
                   $('#email').removeAttr('disabled');
                   $('#form')[0].reset();
               }
            });
        }
    });
});
