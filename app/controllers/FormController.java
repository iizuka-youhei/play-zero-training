package controllers;

import models.User;
import models.CreateForm;
import models.DeleteForm;
import models.UpdateForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import io.ebean.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import com.google.common.collect.Lists;
import java.util.ArrayList;

import static play.libs.Scala.asScala;
    
@Singleton
public class FormController extends Controller {
    private final Form<CreateForm> createForm;
    private final Form<DeleteForm> deleteForm;
    private final Form<UpdateForm> updateForm;

    private MessagesApi messagesApi;
    private List<User> users;
    private User updateUser;

    private final Long master = 0l;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public FormController(FormFactory formFactory, MessagesApi messagesApi) {
        this.createForm = formFactory.form(CreateForm.class);
        this.deleteForm = formFactory.form(DeleteForm.class);
        this.updateForm = formFactory.form(UpdateForm.class);
        this.messagesApi = messagesApi;
    }

    public Result showForm(Http.Request request) {
        this.users = User.finder.all();
        return ok(views.html.board.render(users, deleteForm, createForm, request, messagesApi.preferred(request)));
    }

    public Result fixForm(Http.Request request, Long id) {
        this.updateUser = User.finder.byId(id);
        return ok(views.html.fix.render(users, updateForm, request, messagesApi.preferred(request)));
    }

    public Result create(Http.Request request) {
        final Form<CreateForm> boundForm = createForm.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.board.render(users, deleteForm, boundForm, request, messagesApi.preferred(request)));
        } else {
            CreateForm data = boundForm.get();
            User addUser = new User();
            addUser.setName(data.getName());
            addUser.setText(data.getText());
            Ebean.save(addUser);
            return redirect(routes.FormController.showForm()).flashing("info", "書き込みました");
        }
    }

    public Result delete(Http.Request request) {
        final Form<DeleteForm> boundForm = deleteForm.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.board.render(users, boundForm, createForm, request, messagesApi.preferred(request)));
        } else {
            DeleteForm data = boundForm.get();
            User.finder.ref(data.getId()).delete();
            return redirect(routes.FormController.showForm()).flashing("info", "削除しました");
        }
    }

    public Result update(Http.Request request) {
        final Form<UpdateForm> boundForm = updateForm.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.fix.render(users, boundForm, request, messagesApi.preferred(request)));
        } else {
            UpdateForm data = boundForm.get();
            this.updateUser.setName(data.getName());
            this.updateUser.setText(data.getText());
            Ebean.save(this.updateUser);
            return redirect(routes.FormController.showForm()).flashing("info", "修正しました");
        }
    }

}